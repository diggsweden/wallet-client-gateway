// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.WebApplicationContext;
import se.digg.wallet.gateway.api.v0.model.AuthChallengeRequest;
import se.digg.wallet.gateway.api.v0.model.AuthChallengeResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;
import se.digg.wallet.gateway.api.v0.model.SessionResponse;
import se.digg.wallet.gateway.application.controller.util.WithSpanContext;
import se.digg.wallet.gateway.domain.model.auth.AuthChallengeDto;
import se.digg.wallet.gateway.domain.service.AuthService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static se.digg.wallet.gateway.application.controller.util.ProblemDetailAssertion.assertProblemDetails;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthenticationApiComponentTest {

  private static final String VALIDATION_FAILURE = "/problem-details/field-validation-failure";
  private static final String REQUEST_ARGUMENT_NOT_VALID =
      "/problem-details/request-argument-not-valid";
  private static final String SESSION_ID = "f753681e-0cb1-4a79-8f4a-b0cddaf1561f";
  private static final UUID ACCOUNT_ID = UUID.fromString("61128b3c-ef55-4410-8dff-d8e8bf0cb9a7");
  private static final String KEY_ID = "26862913-ecd0-4d4d-a3d0-9271665d577e";
  private static final String TRACE_ID = "50a41978bfc3c5530ba1676f4be03216";

  @MockitoBean
  private AuthService authService;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private MeterRegistry meterRegistry;

  private RestTestClient client;

  @BeforeEach
  void setUp() { // Inject the configuration
    client = RestTestClient.bindToApplicationContext(context).build();
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
  @WithSpanContext(traceId = TRACE_ID)
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

    assertProblemDetails(problemResponse, HttpStatus.BAD_REQUEST, TRACE_ID,
        REQUEST_ARGUMENT_NOT_VALID, null);
  }

  @ParameterizedTest
  @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "INTERNAL_SERVER_ERROR"})
  @WithSpanContext(traceId = TRACE_ID)
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

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR, TRACE_ID);
  }

  @Test
  @WithSpanContext(traceId = TRACE_ID)
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

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR, TRACE_ID);
    assertThat(problemResponse).isNotNull();
    assertThat(problemResponse.getDetail()).isEqualTo(message);
  }

  @Test
  void servesInitChallenge() {

    var nonce = "test-nonce";
    var challengeDto = new AuthChallengeDto(nonce);
    when(authService.initChallenge(any(), any())).thenReturn(challengeDto);

    var authChallengeResponse = client.get()
        .uri("/public/auth/session/challenge?accountId={0}&keyId={1}", ACCOUNT_ID, KEY_ID)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(AuthChallengeResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(authChallengeResponse).isNotNull();
    assertThat(authChallengeResponse.getNonce()).isNotEmpty().isEqualTo(nonce);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @WithSpanContext(traceId = TRACE_ID)
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

    assertProblemDetails(problemResponse, HttpStatus.BAD_REQUEST, TRACE_ID, VALIDATION_FAILURE,
        "signedJwt");
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
        .expectBody(SessionResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(sessionResponse).isNotNull();
    assertThat(sessionResponse.getSessionId()).isNotEmpty().isEqualTo(SESSION_ID);
  }

  @Test
  void a_rejected_challenge_response_is_counted_as_failure() {

    when(authService.validateChallenge(any())).thenReturn(Optional.empty());
    double before = challengeResponseCount("failure");

    client
        .post()
        .uri("/public/auth/session/response")
        .body(AuthChallengeRequest.builder().signedJwt("signed.jwt.test").build())
        .exchange()
        .expectStatus()
        .isUnauthorized();

    assertThat(challengeResponseCount("failure")).isEqualTo(before + 1);
  }

  @Test
  void a_validated_challenge_response_is_counted_as_success() {

    var validationResult = new AuthService.ValidationResult(ACCOUNT_ID.toString());
    when(authService.validateChallenge(any())).thenReturn(Optional.of(validationResult));
    double before = challengeResponseCount("success");

    client
        .post()
        .uri("/public/auth/session/response")
        .body(AuthChallengeRequest.builder().signedJwt("signed.jwt.test").build())
        .exchange()
        .expectStatus()
        .isOk();

    assertThat(challengeResponseCount("success")).isEqualTo(before + 1);
  }

  private double challengeResponseCount(String outcome) {
    return meterRegistry.get("gateway.auth.challenge_response")
        .tag("outcome", outcome)
        .counter()
        .count();
  }
}
