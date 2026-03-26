// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

import com.github.tomakehurst.wiremock.client.WireMock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.jwk.ECKey;
import com.redis.testcontainers.RedisContainer;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.controller.util.WalletR2psMock;
import se.digg.wallet.gateway.application.model.hsm.HsmRequestDto;
import se.digg.wallet.gateway.application.model.hsm.EcPublicJwkDto;
import se.digg.wallet.gateway.application.model.hsm.RegisterStateRequestDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@WalletR2psMock
@Testcontainers
@ActiveProfiles("test")
class HsmControllerIntegrationTest {

  private static final String REGISTER_STATE_URL = "/wallet-security/v1/device/state";
  private static final String REGISTER_PIN_URL = "/wallet-security/v1/device/pin";
  private static final String CHANGE_PIN_URL = "/wallet-security/v1/device/pin/change";
  private static final String CREATE_SESSION_URL = "/wallet-security/v1/secure-session";
  private static final String CREATE_KEY_URL = "/wallet-security/v1/keys";
  private static final String LIST_KEYS_URL = "/wallet-security/v1/keys/list";
  private static final String DELETE_KEY_URL = "/wallet-security/v1/keys/delete";
  private static final String SIGN_URL = "/wallet-security/v1/keys/sign";
  private static final String TEST_JWT = "eyJhbGciOiJFUzI1NiJ9.test.signature";
  private static final String TEST_CLIENT_ID = UUID.randomUUID().toString();
  private static final String TEST_DEV_AUTH_CODE = "test-dev-auth-code";

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  private static ECKey generatedKeyPair;
  private static final String ACCOUNT_ID = UUID.randomUUID().toString();

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer accountServer;

  @InjectWireMock(WalletR2psMock.NAME)
  private WireMockServer r2psServer;

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
      authenticated = true;
    }
  }

  @Test
  void registerState_returnsCreated() {
    r2psServer.stubFor(WireMock.post("/new_state")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                { "status": "ok", "clientId": "%s", "devAuthorizationCode": "%s" }
                """.formatted(TEST_CLIENT_ID, TEST_DEV_AUTH_CODE))));

    restClient.post()
        .uri(REGISTER_STATE_URL)
        .header("content-type", "application/json")
        .body(new RegisterStateRequestDto(new EcPublicJwkDto("EC", "P-256", "x", "y", "kid"), false, "30d"))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            { "clientId": "%s", "devAuthorizationCode": "%s" }
            """.formatted(TEST_CLIENT_ID, TEST_DEV_AUTH_CODE));
  }

  @Test
  void registerPin_returnsCreated() {
    r2psServer.stubFor(WireMock.post("/service")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                { "jwt": "%s" }
                """.formatted(TEST_JWT))));

    restClient.post()
        .uri(REGISTER_PIN_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT))
        .exchange()
        .expectStatus()
        .isCreated();
  }

  @Test
  void changePin_returnsNoContent() {
    r2psServer.stubFor(WireMock.post("/service")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                { "jwt": "%s" }
                """.formatted(TEST_JWT))));

    restClient.post()
        .uri(CHANGE_PIN_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT))
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  void createSession_returnsCreated() {
    r2psServer.stubFor(WireMock.post("/service")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                { "jwt": "%s" }
                """.formatted(TEST_JWT))));

    restClient.post()
        .uri(CREATE_SESSION_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            { "jwt": "%s" }
            """.formatted(TEST_JWT));
  }

  @Test
  void createKey_returnsCreated() {
    r2psServer.stubFor(WireMock.post("/service")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                { "jwt": "%s" }
                """.formatted(TEST_JWT))));

    restClient.post()
        .uri(CREATE_KEY_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            { "jwt": "%s" }
            """.formatted(TEST_JWT));
  }

  @Test
  void listKeys_returnsOk() {
    r2psServer.stubFor(WireMock.post("/service")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                { "jwt": "%s" }
                """.formatted(TEST_JWT))));

    restClient.post()
        .uri(LIST_KEYS_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json("""
            { "jwt": "%s" }
            """.formatted(TEST_JWT));
  }

  @Test
  void deleteKey_returnsNoContent() {
    r2psServer.stubFor(WireMock.post("/service")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                { "jwt": "%s" }
                """.formatted(TEST_JWT))));

    restClient.post()
        .uri(DELETE_KEY_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT))
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  void sign_returnsOk() {
    r2psServer.stubFor(WireMock.post("/service")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                { "jwt": "%s" }
                """.formatted(TEST_JWT))));

    restClient.post()
        .uri(SIGN_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json("""
            { "jwt": "%s" }
            """.formatted(TEST_JWT));
  }

  @Test
  void registerState_unauthenticated_returnsForbidden() {
    RestTestClient unauthenticated = RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build();

    unauthenticated.post()
        .uri(REGISTER_STATE_URL)
        .header("content-type", "application/json")
        .body(new RegisterStateRequestDto(new EcPublicJwkDto("EC", "P-256", "x", "y", "kid"), false, "30d"))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

}
