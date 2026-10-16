// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.WebApplicationContext;
import se.digg.wallet.gateway.api.v0.model.EcJwkRequest;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;
import se.digg.wallet.gateway.api.v0.model.WalletInstanceAttestationRequest;
import se.digg.wallet.gateway.api.v0.model.WalletInstanceAttestationResponse;
import se.digg.wallet.gateway.application.model.EcJwkRequestTestBuilder;
import se.digg.wallet.gateway.domain.model.attestation.WalletInstanceAttestationBuilder;
import se.digg.wallet.gateway.domain.service.AttestationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static se.digg.wallet.gateway.application.controller.util.ProblemDetailAssertion.assertProblemDetails;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WalletInstanceAttestationApiComponentTest {

  private static final String VALIDATION_FAILURE = "/problem-details/field-validation-failure";
  private static final String TRANSACTION_ID = "a7240655-a568-41c8-8059-7b18859d5d88";
  private static final String JWT = "the.result.jwt";

  @MockitoBean
  private AttestationService attestationService;

  private RestTestClient client;

  @BeforeEach
  void setUp(WebApplicationContext context) { // Inject the configuration
    client = RestTestClient.bindToApplicationContext(context).build();
    MDC.put("transactionId", TRANSACTION_ID);
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
    when(attestationService.createWalletInstanceAttestation(any()))
        .thenThrow(restClientResponseException);

    var problemResponse = client.post()
        .uri("/attestation/v0/wallet-instance-attestations")
        .body(WalletInstanceAttestationRequest.builder()
            .key(EcJwkRequestTestBuilder.withDefaults().build())
            .build())
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(problemResponse).isNotNull();
    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR, TRANSACTION_ID);
    assertThat(problemResponse.getDetail()).isNotEmpty().contains("Remote service failure");
  }

  @Test
  void informs_client_of_unexpected_server_error_problem() {

    final var message = "The cause error message";
    var testException = new UnexpectedException(message);
    when(attestationService.createWalletInstanceAttestation(any())).thenThrow(testException);

    var problemResponse = client.post()
        .uri("/attestation/v0/wallet-instance-attestations")
        .body(WalletInstanceAttestationRequest.builder()
            .key(EcJwkRequestTestBuilder.withDefaults().build())
            .build())
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR, TRANSACTION_ID);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "key.kid",
      "key.kty",
      "key.crv",
      "key.x",
      "key.y"
  })
  void informs_client_of_missing_key_property_problem(String invalidProperty) {

    var problemResponse =
        client
            .post()
            .uri("/attestation/v0/wallet-instance-attestations")
            .body(WalletInstanceAttestationRequest.builder()
                .key(EcJwkRequest.builder().build())
                .build())
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody(ProblemResponse.class)
            .returnResult()
            .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.BAD_REQUEST, TRANSACTION_ID,
        VALIDATION_FAILURE, invalidProperty);
  }

  @Test
  void serves_wallet_instance_attestation() {

    when(attestationService.createWalletInstanceAttestation(any()))
        .thenReturn(WalletInstanceAttestationBuilder.builder().jwt(JWT).build());

    var keyAttestationResponse = client.post()
        .uri("/attestation/v0/wallet-instance-attestations")
        .body(WalletInstanceAttestationRequest.builder()
            .key(EcJwkRequestTestBuilder.withDefaults().build())
            .build())
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(WalletInstanceAttestationResponse.class)
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
}
