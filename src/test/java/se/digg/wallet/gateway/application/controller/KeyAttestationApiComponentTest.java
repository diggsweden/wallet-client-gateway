// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.WebApplicationContext;
import se.digg.wallet.gateway.api.v0.model.EcJwkRequest;
import se.digg.wallet.gateway.api.v0.model.KeyAttestationRequest;
import se.digg.wallet.gateway.api.v0.model.KeyAttestationResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemParameterResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;
import se.digg.wallet.gateway.application.model.EcJwkRequestTestBuilder;
import se.digg.wallet.gateway.domain.model.walletprovider.KeyAttestationBuilder;
import se.digg.wallet.gateway.domain.service.WalletProviderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class KeyAttestationApiComponentTest {

  private static final String VALIDATION_FAILURE = "/problem-details/field-validation-failure";
  private static final String TRANSACTION_ID = "a7240655-a568-41c8-8059-7b18859d5d88";
  private static final String NONCE = "f0c0d595-827e-4f23-921a-f5e6a40fc491";
  private static final String JWT = "the.result.jwt";

  @MockitoBean
  private WalletProviderService walletProviderService;

  private RestTestClient client;

  @BeforeEach
  void setUp(WebApplicationContext context) { // Inject the configuration
    client = RestTestClient.bindToApplicationContext(context).build();
    MDC.put("transactionId", TRANSACTION_ID);
  }

  static List<List<EcJwkRequest>> invalidSetOfEcJwkRequests() {
    var invalidSetOfEcJwks = new ArrayList<List<EcJwkRequest>>();
    invalidSetOfEcJwks.add(null);
    invalidSetOfEcJwks.add(Collections.emptyList());

    return invalidSetOfEcJwks;
  }

  static List<List<EcJwkRequest>> validSetOfEcJwkRequests() {
    var validSetOfEcJwks = new ArrayList<List<EcJwkRequest>>();

    validSetOfEcJwks.add(List.of(EcJwkRequestTestBuilder.withDefaults().build()));
    validSetOfEcJwks.add(List.of(
        EcJwkRequestTestBuilder.withDefaults().build(),
        EcJwkRequestTestBuilder.withDefaults().build()));

    var ecJwksWithNulls = new ArrayList<EcJwkRequest>();
    ecJwksWithNulls.add(EcJwkRequestTestBuilder.withDefaults().build());
    ecJwksWithNulls.add(null);
    ecJwksWithNulls.add(EcJwkRequestTestBuilder.withDefaults().build());
    validSetOfEcJwks.add(ecJwksWithNulls);

    return validSetOfEcJwks;
  }

  @AfterEach
  void cleanUp() {
    MDC.clear();
  }

  @ParameterizedTest
  @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "INTERNAL_SERVER_ERROR"})
  void informs_client_of_remote_service_problem(HttpStatus httpStatus) {

    var restClientResponseException = new RestClientResponseException(
        "The remote error message",
        httpStatus,
        httpStatus.getReasonPhrase(),
        null, null, null);
    when(walletProviderService.createKeyAttestation(any(), any()))
        .thenThrow(restClientResponseException);

    var problemResponse = client.post()
        .uri("/wallet-provider/v0/key-attestations")
        .body(KeyAttestationRequest.builder()
            .keys(List.of(EcJwkRequestTestBuilder.withDefaults().build()))
            .nonce(null)
            .build())
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(problemResponse.getDetail()).isNotEmpty().contains("Remote service failure");
  }

  @Test
  void informs_client_of_unexpected_server_error_problem() {

    final var message = "The cause error message";
    var testException = new KeyAttestationApiComponentTest.UnexpectedException(message);
    when(walletProviderService.createKeyAttestation(any(), any())).thenThrow(testException);

    var problemResponse = client.post()
        .uri("/wallet-provider/v0/key-attestations")
        .body(KeyAttestationRequest.builder()
            .keys(List.of(EcJwkRequestTestBuilder.withDefaults().build()))
            .nonce(null)
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
  @MethodSource("invalidSetOfEcJwkRequests")
  void informs_client_of_missing_keys_problem(List<EcJwkRequest> invalidKeys) {

    var problemResponse = client.post()
        .uri("/wallet-provider/v0/key-attestations")
        .body(KeyAttestationRequest.builder()
            .keys(invalidKeys)
            .nonce(null)
            .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.BAD_REQUEST, VALIDATION_FAILURE, "keys");
  }

  @ParameterizedTest
  @MethodSource("validSetOfEcJwkRequests")
  void serves_key_attestation_without_nonce(List<EcJwkRequest> validKeys) {

    when(walletProviderService.createKeyAttestation(any(), any()))
        .thenReturn(KeyAttestationBuilder.builder().jwt(JWT).build());

    var keyAttestationResponse = client.post()
        .uri("/wallet-provider/v0/key-attestations")
        .body(KeyAttestationRequest.builder()
            .keys(validKeys)
            .nonce(null)
            .build())
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(KeyAttestationResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(keyAttestationResponse).isNotNull();
    assertThat(keyAttestationResponse.getJwt()).isEqualTo(JWT);
  }

  @ParameterizedTest
  @MethodSource("validSetOfEcJwkRequests")
  void serves_key_attestation_with_nonce(List<EcJwkRequest> validKeys) {

    when(walletProviderService.createKeyAttestation(any(), any()))
        .thenReturn(KeyAttestationBuilder.builder().jwt(JWT).build());

    var keyAttestationResponse = client.post()
        .uri("/wallet-provider/v0/key-attestations")
        .body(KeyAttestationRequest.builder()
            .keys(validKeys)
            .nonce(NONCE)
            .build())
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(KeyAttestationResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(keyAttestationResponse).isNotNull();
    assertThat(keyAttestationResponse.getJwt()).isEqualTo(JWT);
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
