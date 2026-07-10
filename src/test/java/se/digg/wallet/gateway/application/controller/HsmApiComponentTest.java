// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.WebApplicationContext;
import se.digg.wallet.gateway.api.v0.model.EcJwkRequest;
import se.digg.wallet.gateway.api.v0.model.EcJwkResponse;
import se.digg.wallet.gateway.api.v0.model.HsmRequest;
import se.digg.wallet.gateway.api.v0.model.HsmResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemParameterResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;
import se.digg.wallet.gateway.api.v0.model.RegisterStateRequest;
import se.digg.wallet.gateway.api.v0.model.RegisterStateResponse;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResultBuilder;
import se.digg.wallet.gateway.domain.model.hsm.EcPublicJwk;
import se.digg.wallet.gateway.domain.model.hsm.EcPublicJwkBuilder;
import se.digg.wallet.gateway.domain.model.hsm.HsmAsyncStatus;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResultBuilder;
import se.digg.wallet.gateway.domain.service.hsm.HsmService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HsmApiComponentTest {

  private static final String VALIDATION_FAILURE = "/problem-details/field-validation-failure";
  private static final String ACCOUNT_ID = "61128b3c-ef55-4410-8dff-d8e8bf0cb9a7";
  private static final String KEY_ID = "26862913-ecd0-4d4d-a3d0-9271665d577e";
  private static final UUID REQUEST_ID = UUID.fromString("8aaf1205-73f2-4a94-8336-da3b321d355f");

  @MockitoBean
  private HsmService hsmService;

  private RestTestClient client;

  @BeforeEach
  void setUp(WebApplicationContext context) { // Inject the configuration
    client = RestTestClient.bindToApplicationContext(context).build();
  }

  @ParameterizedTest
  @NullAndEmptySource
  void informsClientOfOuterRequestJwsProblem(String emptyOuterRequestJws) {

    var problemResponse = client.post()
        .uri("/hsm/v0/requests")
        .body(HsmRequest.builder()
            .outerRequestJws(emptyOuterRequestJws)
            .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.BAD_REQUEST, VALIDATION_FAILURE,
        "outerRequestJws");
  }

  @ParameterizedTest
  @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "INTERNAL_SERVER_ERROR"})
  void requestingHsmWhenRemoteServiceFailsReturnsGenericServerProblem(
      HttpStatus httpStatus) {

    var restClientResponseException = new RestClientResponseException(
        "The remote error message",
        httpStatus,
        httpStatus.getReasonPhrase(),
        null, null, null);
    when(hsmService.submitAsync(any(), any())).thenThrow(restClientResponseException);

    var problemResponse = client.post()
        .uri("/hsm/v0/requests")
        .body(HsmRequest.builder()
            .outerRequestJws("outer.request.jws")
            .build())
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void requestingHsmFailsWithUnexpectedServerErrorReturnsGenericServerProblem() {

    final var message = "The cause error message";
    var testException = new UnexpectedException(message);
    when(hsmService.submitAsync(any(), any())).thenThrow(testException);

    var problemResponse = client.post()
        .uri("/hsm/v0/requests")
        .body(HsmRequest.builder()
            .outerRequestJws("outer.request.jws")
            .build())
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void servesErrorHsmRequest() {

    var status = HsmAsyncStatus.ERROR;
    var hsmOperationResult = HsmOperationResultBuilder.builder()
        .id(REQUEST_ID)
        .status(status)
        .result(null)
        .stateJws(null)
        .resultUrl(null)
        .build();
    when(hsmService.submitAsync(any(), any())).thenReturn(hsmOperationResult);

    authenticateByChallenge();
    var hsmResponse = client.post()
        .uri("/hsm/v0/requests")
        .body(HsmRequest.builder()
            .outerRequestJws("outer.request.jws")
            .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(HsmResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(hsmResponse).isNotNull();
    assertThat(hsmResponse.getId()).isNotNull().isEqualTo(REQUEST_ID);
    assertThat(hsmResponse.getStatus()).isNotNull().isEqualTo(
        se.digg.wallet.gateway.api.v0.model.HsmAsyncStatus.ERROR);
    assertThat(hsmResponse.getResult()).isEmpty();
    assertThat(hsmResponse.getResultUrl().isEmpty());
  }

  @Test
  void servesPendingHsmRequest() {

    var status = HsmAsyncStatus.PENDING;
    var hsmOperationResult = HsmOperationResultBuilder.builder()
        .id(REQUEST_ID)
        .status(status)
        .result(null)
        .stateJws(null)
        .resultUrl("//the.result/url")
        .build();
    when(hsmService.submitAsync(any(), any())).thenReturn(hsmOperationResult);

    authenticateByChallenge();
    var hsmResponse = client.post()
        .uri("/hsm/v0/requests")
        .body(HsmRequest.builder()
            .outerRequestJws("outer.request.jws")
            .build())
        .exchange()
        .expectStatus()
        .isAccepted()
        .expectBody(HsmResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(hsmResponse).isNotNull();
    assertThat(hsmResponse.getId()).isNotNull().isEqualTo(REQUEST_ID);
    assertThat(hsmResponse.getStatus()).isNotNull().isEqualTo(
        se.digg.wallet.gateway.api.v0.model.HsmAsyncStatus.PENDING);
    assertThat(hsmResponse.getResult()).isEmpty();
    assertThat(hsmResponse.getResultUrl().get()).contains(REQUEST_ID.toString());
  }

  @Test
  void servesCompleteHsmRequest() {

    var status = HsmAsyncStatus.COMPLETE;
    var result = "the.complete.result";
    var hsmOperationResult = HsmOperationResultBuilder.builder()
        .id(REQUEST_ID)
        .status(status)
        .result(result)
        .stateJws("the.state.jws")
        .resultUrl("//the.result/url")
        .build();
    when(hsmService.submitAsync(any(), any())).thenReturn(hsmOperationResult);

    authenticateByChallenge();
    var hsmResponse = client.post()
        .uri("/hsm/v0/requests")
        .body(HsmRequest.builder()
            .outerRequestJws("outer.request.jws")
            .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(HsmResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(hsmResponse).isNotNull();
    assertThat(hsmResponse.getId()).isNotNull().isEqualTo(REQUEST_ID);
    assertThat(hsmResponse.getStatus()).isNotNull().isEqualTo(
        se.digg.wallet.gateway.api.v0.model.HsmAsyncStatus.COMPLETE);
    assertThat(hsmResponse.getResult()).isPresent().get().isEqualTo(result);
    assertThat(hsmResponse.getResultUrl().get()).contains(REQUEST_ID.toString());
  }

  @Test
  void pollErrorHsmResponseReturnsEmpty() {

    var status = HsmAsyncStatus.ERROR;
    var hsmOperationResult = HsmOperationResultBuilder.builder()
        .id(REQUEST_ID)
        .status(status)
        .result(null)
        .stateJws(null)
        .resultUrl("//the.result/url")
        .build();
    when(hsmService.getAsyncResult(any(), any())).thenReturn(hsmOperationResult);

    authenticateByChallenge();
    var hsmResponse = client.get()
        .uri("/hsm/v0/requests/{0}", REQUEST_ID)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(HsmResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(hsmResponse).isNotNull();
    assertThat(hsmResponse.getId()).isNotNull().isEqualTo(REQUEST_ID);
    assertThat(hsmResponse.getStatus()).isNotNull().isEqualTo(
        se.digg.wallet.gateway.api.v0.model.HsmAsyncStatus.ERROR);
    assertThat(hsmResponse.getResultUrl().get()).contains(REQUEST_ID.toString());
  }

  @Test
  void pollPendingHsmResponseReturnsEmpty() {

    var status = HsmAsyncStatus.PENDING;
    var hsmOperationResult = HsmOperationResultBuilder.builder()
        .id(REQUEST_ID)
        .status(status)
        .result(null)
        .stateJws("the.state.jws")
        .resultUrl("//the.result/url")
        .build();
    when(hsmService.getAsyncResult(any(), any())).thenReturn(hsmOperationResult);

    authenticateByChallenge();
    var hsmResponse = client.get()
        .uri("/hsm/v0/requests/{0}", REQUEST_ID)
        .exchange()
        .expectStatus()
        .isAccepted()
        .expectBody(HsmResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(hsmResponse).isNotNull();
    assertThat(hsmResponse.getId()).isNotNull().isEqualTo(REQUEST_ID);
    assertThat(hsmResponse.getStatus()).isNotNull().isEqualTo(
        se.digg.wallet.gateway.api.v0.model.HsmAsyncStatus.PENDING);
    assertThat(hsmResponse.getResultUrl().get()).contains(REQUEST_ID.toString());
  }

  @Test
  void pollCompleteHsmResponseReturnsResult() {

    var status = HsmAsyncStatus.COMPLETE;
    var result = "the.complete.result";
    var hsmOperationResult = HsmOperationResultBuilder.builder()
        .id(REQUEST_ID)
        .status(status)
        .result(result)
        .stateJws("the.state.jws")
        .resultUrl("//the.result/url")
        .build();
    when(hsmService.getAsyncResult(any(), any())).thenReturn(hsmOperationResult);

    authenticateByChallenge();
    var hsmResponse = client.get()
        .uri("/hsm/v0/requests/{0}", REQUEST_ID)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(HsmResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(hsmResponse).isNotNull();
    assertThat(hsmResponse.getId()).isNotNull().isEqualTo(REQUEST_ID);
    assertThat(hsmResponse.getStatus()).isNotNull().isEqualTo(
        se.digg.wallet.gateway.api.v0.model.HsmAsyncStatus.COMPLETE);
    assertThat(hsmResponse.getResultUrl().get()).contains(REQUEST_ID.toString());
  }

  @Test
  void saveDeviceStateWithoutDeviceKeyReturnsDeviceKeyProblem() {

    var problemResponse = client.post()
        .uri("/hsm/v0/device-states")
        .body(RegisterStateRequest.builder()
            .deviceKey(null)
            .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.BAD_REQUEST, VALIDATION_FAILURE, "deviceKey");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "deviceKey.kid",
      "deviceKey.kty",
      "deviceKey.crv",
      "deviceKey.x",
      "deviceKey.y"
  })
  void informsClientOfDeviceKeyParameterProblem(String invalidProperty) {

    var problemResponse = client.post()
        .uri("/hsm/v0/device-states")
        .body(RegisterStateRequest.builder()
            .deviceKey(EcJwkRequest.builder().build())
            .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.BAD_REQUEST, VALIDATION_FAILURE,
        invalidProperty);
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {
      " ",
      "1 hour",
      "30d",
      "one year",
      "P30"
  })
  void informsClientOfTtlParameterProblem(String invalidTtl) {

    var problemResponse = client.post()
        .uri("/hsm/v0/device-states")
        .body(RegisterStateRequest.builder()
            .deviceKey(defaultKeyRequest().build())
            .ttl(invalidTtl)
            .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.BAD_REQUEST, VALIDATION_FAILURE, "ttl");
  }

  @ParameterizedTest
  @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "INTERNAL_SERVER_ERROR"})
  void saveDeviceStateWhenRemoteServiceFailingReturnsGenericServerProblem(
      HttpStatus httpStatus) {

    var restClientResponseException = new RestClientResponseException(
        "The remote error message",
        httpStatus,
        httpStatus.getReasonPhrase(),
        null, null, null);
    when(hsmService.registerState(any(), any())).thenThrow(restClientResponseException);

    var problemResponse = client.post()
        .uri("/hsm/v0/device-states")
        .body(RegisterStateRequest.builder()
            .deviceKey(defaultKeyRequest().build())
            .build())
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void saveDeviceStateFailingWithUnexpectedServerErrorReturnsGenericServerProblem() {

    final var message = "The cause error message";
    var testException = new AccountApiComponentTest.UnexpectedException(message);
    when(hsmService.registerState(any(), any())).thenThrow(testException);

    var problemResponse = client.post()
        .uri("/hsm/v0/device-states")
        .body(RegisterStateRequest.builder()
            .deviceKey(defaultKeyRequest().build())
            .build())
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"PT5H30M", "P30D", "P3M", "P1Y"})
  void saveDeviceStateReturnsRegistration(String optionalTtl) {

    var challengeAuthToken = new ChallengeResponseAuthentication(ACCOUNT_ID);
    challengeAuthToken.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(challengeAuthToken);

    var clientId = "the-client-id";
    var status = "status";
    var devAuthCode = "dev-auth-code";
    var opaqueServerId = "opaque.server.id";
    var serverJwsPublicKey = EcPublicJwkBuilder.builder()
        .kid("the-kid")
        .kty("the-kty")
        .crv("the-crv")
        .x("x-value")
        .y("y-value")
        .build();
    var deviceStateRegistrationResult = DeviceStateRegistrationResultBuilder.builder()
        .clientId(clientId)
        .devAuthorizationCode(devAuthCode)
        .opaqueServerId(opaqueServerId)
        .serverJwsPublicKey(serverJwsPublicKey)
        .stateJws("the.state.jws")
        .status(status)
        .build();
    when(hsmService.registerState(any(), any())).thenReturn(deviceStateRegistrationResult);

    var registerStateResponse = client.post()
        .uri("/hsm/v0/device-states")
        .body(RegisterStateRequest.builder()
            .deviceKey(defaultKeyRequest().build())
            .ttl(optionalTtl)
            .build())
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(RegisterStateResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(registerStateResponse).isNotNull();
    assertThat(registerStateResponse.getStatus()).isNotEmpty().isEqualTo(status);
    assertThat(registerStateResponse.getDevAuthorizationCode()).isPresent().get()
        .isEqualTo(devAuthCode);
    assertThat(registerStateResponse.getOpaqueServerId()).isPresent().get()
        .isEqualTo(opaqueServerId);
    assertThat(registerStateResponse.getServerJwsPublicKey()).isPresent().get()
        .isEqualTo(toKeyResponse(serverJwsPublicKey));
  }

  public static class UnexpectedException extends RuntimeException {

    public UnexpectedException(String message) {
      super(message);
    }
  }

  private static void authenticateByChallenge() {
    var challengeAuthToken = new ChallengeResponseAuthentication(ACCOUNT_ID);
    challengeAuthToken.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(challengeAuthToken);
  }

  private static EcJwkRequest.Builder defaultKeyRequest() {
    return EcJwkRequest.builder()
        .kid(KEY_ID)
        .kty("EC")
        .crv("P-256")
        .x("1fH0eqXgMMwCIafNaDc1axdCjLlw7zpTLvLWjpPvhEc")
        .y("5qOejJs7BK-jLingaUTEhBrzP_YPyHfptS5yWE98I40");
  }

  private static EcJwkResponse toKeyResponse(EcPublicJwk jwkDto) {
    return EcJwkResponse.builder()
        .kid(jwkDto.kid())
        .kty(jwkDto.kty())
        .crv(jwkDto.crv())
        .x(jwkDto.x())
        .y(jwkDto.y())
        .build();
  }

  private static void assertProblemDetails(ProblemResponse problemResponse,
      HttpStatus expectedHttpStatus) {

    assertProblemDetails(problemResponse, expectedHttpStatus, null, null);
  }

  private static void assertProblemDetails(ProblemResponse problemResponse,
      HttpStatus expectedHttpStatus,
      @Nullable String expectedType,
      @Nullable String expectedInvalidParameterProperty) {

    assertThat(problemResponse).isNotNull();
    assertThat(problemResponse.getStatus()).isEqualTo(expectedHttpStatus.value());
    assertThat(problemResponse.getTitle()).isNotEmpty();
    assertThat(problemResponse.getDetail()).isPresent();
    assertThat(problemResponse.getInstance()).isNotEmpty();
    assertThat(problemResponse.getType()).isPresent();
    if (expectedType != null) {
      assertThat(problemResponse.getType()).get().isEqualTo(expectedType);
    }

    if (expectedInvalidParameterProperty != null) {
      assertThat(problemResponse.getInvalidParameters()).isNotEmpty();
      assertThat(expectedInvalidParameterProperty).isIn(problemResponse.getInvalidParameters()
          .stream()
          .map(ProblemParameterResponse::getProperty)
          .map(value -> value.orElse(null))
          .filter(Objects::nonNull)
          .toList());
    }
  }
}
