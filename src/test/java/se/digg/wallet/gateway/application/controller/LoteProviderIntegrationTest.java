// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.jwk.ECKey;
import com.redis.testcontainers.RedisContainer;
import java.net.URI;
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
import se.digg.wallet.gateway.api.v0.TrustApi;
import se.digg.wallet.gateway.api.v0.model.LoteProviderItemResponse;
import se.digg.wallet.gateway.api.v0.model.LoteProvidersResponse;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "properties.lote-providers[0].id=Test 1",
        "properties.lote-providers[0].url=https://first-url.test",
        "properties.lote-providers[1].id=Test 2",
        "properties.lote-providers[1].url=https://second-url.test"
    })
@WalletAccountMock
@Testcontainers
@ActiveProfiles("test")
class LoteProviderIntegrationTest {

  private static final String ACCOUNT_ID = UUID.randomUUID().toString();

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  private static ECKey generatedKeyPair;

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer accountServer;

  @Autowired
  private ApplicationConfig applicationConfig;

  private RestTestClient restClient;
  private RestTestClient authenticatedRestClient;

  @BeforeAll
  static void beforeAll() throws Exception {
    generatedKeyPair = AuthUtil.generateKey();
  }

  @BeforeEach
  void beforeEach() throws Exception {
    var loginClient = RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build();
    authenticatedRestClient = AuthUtil.login(
        accountServer, port, loginClient, ACCOUNT_ID, generatedKeyPair);
    restClient = RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build();
  }

  @Test
  void serves_configured_lote_providers() {
    var response = authenticatedRestClient.get()
        .uri(TrustApi.PATH_GET_LOTE_PROVIDERS)
        .exchange()
        .expectStatus().isOk()
        .expectBody(LoteProvidersResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(response).isNotNull();
    assertThat(response.getItems()).containsExactly(
        new LoteProviderItemResponse("Test 1", URI.create("https://first-url.test")),
        new LoteProviderItemResponse("Test 2", URI.create("https://second-url.test")));
  }

  @Test
  void rejects_request_without_challenge_response_session() {
    restClient.get()
        .uri(TrustApi.PATH_GET_LOTE_PROVIDERS)
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  void rejects_request_without_apikey() {
    var clientWithoutApiKey = authenticatedRestClient.mutate()
        .defaultHeaders(headers -> headers.remove(SecurityConfig.API_KEY_HEADER))
        .build();

    clientWithoutApiKey.get()
        .uri(TrustApi.PATH_GET_LOTE_PROVIDERS)
        .exchange()
        .expectStatus().isForbidden();
  }
}
