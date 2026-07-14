// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.WebApplicationContext;
import se.digg.wallet.gateway.api.v0.model.AuthChallengeDto;
import se.digg.wallet.gateway.api.v0.model.AuthChallengeRequest;
import se.digg.wallet.gateway.api.v0.model.AuthChallengeResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemParameterResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;
import se.digg.wallet.gateway.domain.service.auth.AuthService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationApiComponentTest {

  private static final String VALIDATION_FAILURE = "/problem-details/field-validation-failure";
  private static final String REQUEST_ARGUMENT_NOT_VALID =
      "/problem-details/request-argument-not-valid";
  private static final String SESSION_ID = "f753681e-0cb1-4a79-8f4a-b0cddaf1561f";
  private static final UUID ACCOUNT_ID = UUID.fromString("61128b3c-ef55-4410-8dff-d8e8bf0cb9a7");
  private static final String KEY_ID = "26862913-ecd0-4d4d-a3d0-9271665d577e";
  private static final String TRANSACTION_ID = "a7240655-a568-41c8-8059-7b18859d5d88";

  @MockitoBean
  private AuthService authService;

  @Autowired
  private WebApplicationContext context;

  private RestTestClient client;

  @BeforeEach
  void setUp() { // Inject the configuration
    client = RestTestClient.bindToApplicationContext(context).build();
    MDC.put("transactionId", TRANSACTION_ID);
  }

  @AfterEach
  void cleanUp() {
    MDC.clear();
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {
      "accountId=",
      "accountId=abc123",
      "keyId=",
      "keyId=def456",
      "accountId=&keyId=",
      "accountId=abc123&keyId=",
      "accountId=&keyId=def456"
  })
  void informsClientOfChallengeRequestArgumentProblem(String queryString) {

    var problemResponse = client.get()
        .uri(uriBuilder -> uriBuilder
            .path("/public/auth/session/challenge")
            .query(queryString)
            .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.BAD_REQUEST, REQUEST_ARGUMENT_NOT_VALID, null);
  }

  @ParameterizedTest
  @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "INTERNAL_SERVER_ERROR"})
  void initChallengeFailingWithRemoteServiceReturnsGenericServerProblem(HttpStatus httpStatus) {

    var restClientResponseException = new RestClientResponseException(
        "The remote error message",
        httpStatus,
        httpStatus.getReasonPhrase(),
        null, null, null);
    when(authService.initChallenge(any(), any())).thenThrow(restClientResponseException);

    var problemResponse = client.get()
        .uri("/public/auth/session/challenge?accountId={0}&keyId={1}", ACCOUNT_ID, KEY_ID)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void initChallengeFailingWithUnexpectedServerErrorReturnsGenericServerProblem() {

    final var message = "The cause error message";
    var testException = new AccountApiComponentTest.UnexpectedException(message);
    when(authService.initChallenge(any(), any())).thenThrow(testException);

    var problemResponse = client.get()
        .uri("/public/auth/session/challenge?accountId={0}&keyId={1}", ACCOUNT_ID, KEY_ID)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(problemResponse.getDetail()).get().isEqualTo(message);
  }

  @Test
  void servesInitChallenge() {

    var nonce = "test-nonce";
    var challengeDto = new se.digg.wallet.gateway.application.model.auth.AuthChallengeDto(nonce);
    when(authService.initChallenge(any(), any())).thenReturn(challengeDto);

    var authChallengeResponse = client.get()
        .uri("/public/auth/session/challenge?accountId={0}&keyId={1}", ACCOUNT_ID, KEY_ID)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(AuthChallengeDto.class)
        .returnResult()
        .getResponseBody();

    assertThat(authChallengeResponse).isNotNull();
    assertThat(authChallengeResponse.getNonce()).isPresent().get().isEqualTo(nonce);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void informsClientOfMissingSignedJwtProblem(String emptySignedJwt) {

    var sessionRequest = AuthChallengeRequest.builder()
        .signedJwt(emptySignedJwt)
        .build();
    var problemResponse = client.post()
        .uri("/public/auth/session/response")
        .body(sessionRequest)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.BAD_REQUEST, VALIDATION_FAILURE, "signedJwt");
  }

  @Test
  void invalidChallengeReturnsUnauthorized() {

    when(authService.validateChallenge(any())).thenReturn(Optional.empty());

    var sessionRequest = AuthChallengeRequest.builder()
        .signedJwt("signed.jwt.test")
        .build();
    client
        .post()
        .uri("/public/auth/session/response")
        .body(sessionRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void validChallengeServesSession() {

    var validationResult = new AuthService.ValidationResult(ACCOUNT_ID.toString());
    when(authService.validateChallenge(any())).thenReturn(Optional.of(validationResult));

    MockHttpSession mockHttpSession = new MockHttpSession(context.getServletContext(), SESSION_ID);

    RequestPostProcessor sessionPostProcessor = request -> {
      request.setSession(mockHttpSession);
      return request;
    };

    var mockSessionClient = RestTestClient.bindToApplicationContext(context)
        .configureServer(builder -> builder.defaultRequest(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/")
                .with(sessionPostProcessor)))
        .build();

    var sessionRequest = AuthChallengeRequest.builder()
        .signedJwt("signed.jwt.test")
        .build();
    var sessionResponse = mockSessionClient
        .post()
        .uri("/public/auth/session/response")
        .body(sessionRequest)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(AuthChallengeResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(sessionResponse).isNotNull();
    assertThat(sessionResponse.getSessionId()).isNotEmpty().isEqualTo(SESSION_ID);
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
    assertThat(problemResponse.getTransactionId()).isPresent().get().isEqualTo(TRANSACTION_ID);
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
