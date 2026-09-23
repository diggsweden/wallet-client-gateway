// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Json;
import com.nimbusds.jose.jwk.ECKey;
import com.redis.testcontainers.RedisContainer;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.api.v0.model.EcJwkRequest;
import se.digg.wallet.gateway.api.v0.model.KeyAttestationRequest;
import se.digg.wallet.gateway.api.v0.model.KeyAttestationResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.controller.util.WalletProviderMock;
import se.digg.wallet.gateway.application.model.EcJwkRequestTestBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@WalletProviderMock
@Testcontainers
@ActiveProfiles("test")
public class KeyAttestationIntegrationTest {

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  private static ECKey generatedKeyPair;
  private static final String ACCOUNT_ID = "eb4a4bf9-1af0-4991-9c57-42c5f1c5904a";
  private static final String NONCE = "the-nonce-value";
  private static final String SIGNED_JWT = "the.signed.jwt";

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private static WireMockServer accountServer;

  @InjectWireMock(WalletProviderMock.NAME)
  private static WireMockServer providerServer;

  @Autowired
  private ApplicationConfig applicationConfig;

  @Autowired
  private ObjectMapper objectMapper;

  private RestTestClient restClient;
  private boolean authenticated = false;

  @BeforeAll
  static void beforeAll() throws Exception {
    generatedKeyPair = AuthUtil.generateKey();
  }

  @BeforeEach
  void beforeEach() throws Exception {
    if (!authenticated) {
      restClient = RestTestClient.bindToServer()
          .baseUrl("http://localhost:" + port)
          .build();
      restClient = AuthUtil.login(accountServer, port, restClient, ACCOUNT_ID, generatedKeyPair);
      restClient = restClient.mutate()
          .defaultHeader(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
          .build();
      authenticated = true;
    }
    providerServer.resetAll();
  }

  @Test
  void unauthorized_request_returns_forbidden() {

    RestTestClient unauthenticatedClient = RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build();

    unauthenticatedClient.post()
        .uri("/attestation/v0/key-attestations")
        .exchange()
        .expectStatus().isForbidden();
  }

  @ParameterizedTest
  @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "INTERNAL_SERVER_ERROR"})
  void informs_client_of_remote_service_failure_problem(HttpStatus httpStatus) {

    var ecJwkRequests = List.of(EcJwkRequestTestBuilder.withDefaults().build());
    stubRemoteKeyAttestationRequestFailure(ecJwkRequests, httpStatus);

    var keyAttestationRequest = KeyAttestationRequest.builder()
        .keys(ecJwkRequests)
        .nonce(NONCE)
        .build();

    var problemResponse = restClient.post()
        .uri("/attestation/v0/key-attestations")
        .body(keyAttestationRequest)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(problemResponse).isNotNull();
    assertThat(problemResponse.getStatus())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(problemResponse.getTitle())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    assertThat(problemResponse.getDetail()).contains("Remote service failure");
  }

  @Test
  void serves_key_attestation() {

    var ecJwkRequests = List.of(EcJwkRequestTestBuilder.withDefaults().build());
    stubRemoteKeyAttestationRequest(ecJwkRequests);

    var keyAttestationRequest = KeyAttestationRequest.builder()
        .keys(ecJwkRequests)
        .nonce(NONCE)
        .build();

    var keyAttestationResponse = restClient.post()
        .uri("/attestation/v0/key-attestations")
        .body(keyAttestationRequest)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(KeyAttestationResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(keyAttestationResponse).isNotNull();
  }

  private void stubRemoteKeyAttestationRequestFailure(List<EcJwkRequest> ecJwkRequests,
      HttpStatus httpStatus) {

    var remoteKeyAttestationItemsRequest = ecJwkRequests.stream()
        .map(ecJwkRequest -> {
          try {
            return objectMapper.writeValueAsString(ecJwkRequest);
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        })
        .map(jwkJson -> se.digg.wallet.gateway.client.provider.v0.model.KeyAttestationItem.builder()
            .jwk(jwkJson)
            .build())
        .toList();

    var remoteKeyAttestationRequest =
        se.digg.wallet.gateway.client.provider.v0.model.KeyAttestationRequest.builder()
            .jwks(remoteKeyAttestationItemsRequest)
            .nonce(NONCE)
            .build();

    var remoteResponse =
        se.digg.wallet.gateway.client.provider.v0.model.ProblemResponse.builder()
            .status(httpStatus.value())
            .title(httpStatus.getReasonPhrase())
            .build();

    providerServer.stubFor(post(urlEqualTo("/v0/key-attestations"))
        .withRequestBody(equalToJson(Json.write(remoteKeyAttestationRequest)))
        .willReturn(aResponse()
            .withStatus(httpStatus.value())
            .withHeader("content-type", "application/json+problem")
            .withBody(Json.write(remoteResponse))));
  }

  private void stubRemoteKeyAttestationRequest(List<EcJwkRequest> ecJwkRequests) {

    var remoteKeyAttestationItemsRequest = ecJwkRequests.stream()
        .map(ecJwkRequest -> {
          try {
            return objectMapper.writeValueAsString(ecJwkRequest);
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        })
        .map(jwkJson -> se.digg.wallet.gateway.client.provider.v0.model.KeyAttestationItem.builder()
            .jwk(jwkJson)
            .build())
        .toList();

    var remoteKeyAttestationRequest =
        se.digg.wallet.gateway.client.provider.v0.model.KeyAttestationRequest.builder()
            .jwks(remoteKeyAttestationItemsRequest)
            .nonce(NONCE)
            .build();

    var remoteResponse =
        se.digg.wallet.gateway.client.provider.v0.model.KeyAttestationResponse.builder()
            .keyAttestation(SIGNED_JWT)
            .build();

    providerServer.stubFor(post(urlEqualTo("/v0/key-attestations"))
        .withRequestBody(equalToJson(Json.write(remoteKeyAttestationRequest)))
        .willReturn(aResponse()
            .withStatus(HttpStatus.CREATED.value())
            .withHeader("content-type", "application/json")
            .withBody(Json.write(remoteResponse))));
  }
}
