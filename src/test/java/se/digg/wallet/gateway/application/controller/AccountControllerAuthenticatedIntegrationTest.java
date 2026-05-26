// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.jwk.ECKey;
import com.redis.testcontainers.RedisContainer;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.api.v0.model.SecurityEnvelopeRequest;
import se.digg.wallet.gateway.api.v0.model.SecurityEnvelopeType;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.model.KeyRequestTestBuilder;
import se.digg.wallet.gateway.client.account.v0.model.KeyRequest;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@Testcontainers
@ActiveProfiles("test")
class AccountControllerAuthenticatedIntegrationTest {

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  private static final String ACCOUNT_ID = UUID.randomUUID().toString();
  private static ECKey generatedKeyPair;

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer accountServer;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ApplicationConfig applicationConfig;

  private RestTestClient restClient;
  private boolean authenticated = false;

  @BeforeAll
  public static void beforeAll() throws Exception {
    generatedKeyPair = AuthUtil.generateKey();
  }

  @BeforeEach
  public void beforeEach() throws Exception {
    if (!authenticated) {
      restClient = RestTestClient.bindToServer()
          .baseUrl("http://localhost:" + port)
          .build();
      restClient = AuthUtil.login(accountServer, port, restClient, ACCOUNT_ID, generatedKeyPair);
      authenticated = true;
    }
  }

  @Test
  void testAddWalletKey() throws Exception {
    var expectedRequest = KeyRequest.builder()
        .kty("KTY").kid("KID").alg("ALG").use("USE")
        .crv("CRV").x("X").y("Y")
        .build();

    accountServer.stubFor(post("/v0/accounts/" + ACCOUNT_ID + "/wallet-keys")
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(expectedRequest)))
        .willReturn(aResponse().withStatus(201)));

    var response = restClient.post()
        .uri("/v0/accounts/wallet-keys")
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(KeyRequestTestBuilder.withDefaults().build())
        .exchange();

    response.expectStatus().isCreated();
  }

  @Test
  void returnProblemWithStatus400WhenCreateWalletKeyLacksRequiredKid() throws Exception {

    var emptyKeyRequest = KeyRequest.builder()
        .kid(null)
        .build();

    var response = restClient.post()
        .uri("/v0/accounts/wallet-keys")
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(emptyKeyRequest)
        .exchange();

    var expectedStatus = 400;
    response.expectStatus().isEqualTo(expectedStatus)
        .expectBody()
        .jsonPath("$.status").isEqualTo(expectedStatus)
        .jsonPath("$.type").isNotEmpty()
        .jsonPath("$.title").exists()
        .jsonPath("$.detail").exists()
        .jsonPath("$.instance").exists()
        .jsonPath("$.invalid-parameters").isNotEmpty();
  }

  @Test
  void returnsProblemWithStatus500WhenSaveWalletKeyDownstreamFails() {
    accountServer.stubFor(post("/v0/accounts/" + ACCOUNT_ID + "/wallet-keys")
        .willReturn(aResponse().withStatus(404)));

    var response = restClient.post()
        .uri("/v0/accounts/wallet-keys")
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(KeyRequestTestBuilder.withDefaults().build())
        .exchange();

    var expectedStatus = 500;
    response.expectStatus().isEqualTo(expectedStatus)
        .expectBody()
        .jsonPath("$.status").isEqualTo(expectedStatus)
        .jsonPath("$.type").exists()
        .jsonPath("$.title").exists()
        .jsonPath("$.detail").exists()
        .jsonPath("$.instance").exists();
  }

  @Test
  void testAddSecurityEnvelope() {
    var envelopeContent = "envelope-content";

    var expectedDownstreamRequest =
        se.digg.wallet.gateway.client.account.v0.model.SecurityEnvelopeRequest.builder()
            .content(envelopeContent)
            .build();

    accountServer.stubFor(post("/v0/accounts/" + ACCOUNT_ID + "/security-envelopes")
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(expectedDownstreamRequest)))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "application/json")
            .withBody("{}")));

    var response = restClient.post()
        .uri("/v0/accounts/security-envelopes")
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(SecurityEnvelopeRequest.builder()
            .type(SecurityEnvelopeType.SIGN)
            .content(envelopeContent)
            .build())
        .exchange();

    response.expectStatus().isCreated();
  }

  @Test
  void returnsProblemWithStatus500WhenSaveSecurityEnvelopeDownstreamFails() {
    accountServer.stubFor(post("/v0/accounts/" + ACCOUNT_ID + "/security-envelopes")
        .willReturn(aResponse().withStatus(404)));

    var response = restClient.post()
        .uri("/v0/accounts/security-envelopes")
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(SecurityEnvelopeRequest.builder()
            .type(SecurityEnvelopeType.SIGN)
            .content("content")
            .build())
        .exchange();

    var expectedStatus = 500;
    response.expectStatus().isEqualTo(expectedStatus)
        .expectBody()
        .jsonPath("$.status").isEqualTo(expectedStatus)
        .jsonPath("$.type").exists()
        .jsonPath("$.title").exists()
        .jsonPath("$.detail").exists()
        .jsonPath("$.instance").exists();
  }
}
