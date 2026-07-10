// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import java.util.Objects;
import java.util.UUID;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.WebApplicationContext;
import se.digg.wallet.gateway.api.v0.model.ProblemParameterResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;
import se.digg.wallet.gateway.api.v0.model.WuaResponse;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;
import se.digg.wallet.gateway.application.model.wua.WuaDto;
import se.digg.wallet.gateway.domain.service.wua.WuaService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WuaApiComponentTest {

  private static final String ACCOUNT_ID = "61128b3c-ef55-4410-8dff-d8e8bf0cb9a7";

  @MockitoBean
  private WuaService wuaService;

  private RestTestClient client;

  @BeforeEach
  void setUp(WebApplicationContext context) { // Inject the configuration
    client = RestTestClient.bindToApplicationContext(context).build();
  }

  @Test
  void createWuaWithInvalidSessionReturnsBadRequest() {

    client.post()
        .uri("/wua")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .returnResult();
  }

  @Test
  void createWuaWithEmptyNonceReturnsBadRequest() {

    authenticateByChallenge();
    client.post()
        .uri("/wua?nonce=")
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void createWuaWithNonExistingAccountReturnsServerProblem() {

    var challengeAuthToken = new ChallengeResponseAuthentication(ACCOUNT_ID);
    challengeAuthToken.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(challengeAuthToken);

    var restClientResponseException = new RestClientResponseException(
        "Requested resource could not be found",
        HttpStatus.NOT_FOUND,
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        null, null, null);
    when(wuaService.createWua(any(), any())).thenThrow(restClientResponseException);

    ChallengeResponseAuthentication mockAuth = new ChallengeResponseAuthentication(ACCOUNT_ID);

    var problemResponse = client.post()
        .uri("/wua")
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ParameterizedTest
  @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "INTERNAL_SERVER_ERROR"})
  void creatingWuaWhenRemoteServiceFailsReturnsGenericServerProblem(
      HttpStatus httpStatus) {

    var restClientResponseException = new RestClientResponseException(
        "The remote error message",
        httpStatus,
        httpStatus.getReasonPhrase(),
        null, null, null);
    when(wuaService.createWua(any(), any())).thenThrow(restClientResponseException);

    authenticateByChallenge();
    var problemResponse = client.post()
        .uri("/wua")
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void unexpectedServerErrorReturnsGenericServerProblem() {

    final var message = "The cause error message";
    var testException = new UnexpectedException(message);
    when(wuaService.createWua(any(), any())).thenThrow(testException);

    authenticateByChallenge();
    var problemResponse = client.post()
        .uri("/wua")
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private static void authenticateByChallenge() {
    var challengeAuthToken = new ChallengeResponseAuthentication(ACCOUNT_ID);
    challengeAuthToken.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(challengeAuthToken);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = "nonce=123")
  void servesWua(String queryString) {

    var challengeAuthToken = new ChallengeResponseAuthentication(ACCOUNT_ID);
    challengeAuthToken.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(challengeAuthToken);

    var jwt = "the.response.jwt";
    var wuaDto = new WuaDto(jwt);
    when(wuaService.createWua(any(), any())).thenReturn(wuaDto);

    var wuaResponse = client.post()
        .uri("/wua?{0}", queryString)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(WuaResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(wuaResponse).isNotNull();
    assertThat(wuaResponse.getJwt()).isNotEmpty().isEqualTo(jwt);
  }

  public static class UnexpectedException extends RuntimeException {

    public UnexpectedException(String message) {
      super(message);
    }
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
