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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.controller.util.WalletR2psMock;
import se.digg.wallet.gateway.api.v0.model.HsmRequestDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@WalletR2psMock
@Testcontainers
@ActiveProfiles("test")
class HsmControllerIntegrationTest {

  private static final String REGISTER_STATE_URL = "/v0/device/state";
  private static final String REGISTER_PIN_URL = "/v0/device/pin";
  private static final String CHANGE_PIN_URL = "/v0/device/pin";
  private static final String CREATE_SESSION_URL = "/v0/secure-session";
  private static final String CREATE_KEY_URL = "/v0/keys";
  private static final String LIST_KEYS_URL = "/v0/keys/list";
  private static final String DELETE_KEY_URL = "/v0/keys/delete";
  private static final String SIGN_URL = "/v0/keys/sign";
  private static final String HSM_REQUESTS_URL = "/v0/hsm/requests";
  private static final String REGISTER_PIN_ASYNC_URL = "/v0/device/pin/requests";
  private static final String LIST_KEYS_ASYNC_URL = "/v0/keys/list/requests";
  private static final String SIGN_ASYNC_URL = "/v0/keys/sign/requests";
  private static final String TEST_JWT = "eyJhbGciOiJFUzI1NiJ9.test.signature";
  private static final String TEST_CLIENT_ID = UUID.randomUUID().toString();
  private static final String TEST_DEV_AUTH_CODE = "test-dev-auth-code";
  private static final String TEST_CORRELATION_ID = UUID.randomUUID().toString();
  private static final String BFF_RESULT_URL = "http://wallet-bff/hsm/v1/requests/";

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

  @Autowired
  private ApplicationConfig applicationConfig;

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
  }

  @Test
  void registersDeviceState() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/device-states")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "status": "ok",
                  "clientId": "%s",
                  "devAuthorizationCode": "%s",
                  "serverJwsPublicKey": {
                    "kty": "EC",
                    "crv": "P-256",
                    "x": "x",
                    "y": "y"
                  },
                  "opaqueServerId": "server"
                }
                """.formatted(TEST_CLIENT_ID, TEST_DEV_AUTH_CODE))));

    var registerStateRequest = se.digg.wallet.gateway.api.v0.model.RegisterStateRequestDto.builder()
        .publicKey(se.digg.wallet.gateway.api.v0.model.EcPublicJwkDto.builder()
            .kty("EC")
            .crv("P-256")
            .x("x")
            .y("y")
            .kid("kid")
            .build())
        .overwrite(false)
        .ttl("30d")
        .build();

    restClient.post()
        .uri(REGISTER_STATE_URL)
        .header("content-type", "application/json")
        .body(registerStateRequest)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            { "clientId": "%s", "devAuthorizationCode": "%s" }
            """.formatted(TEST_CLIENT_ID, TEST_DEV_AUTH_CODE));
  }

  @Test
  void registersPin() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/operations")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "text/plain")
            .withBody(TEST_JWT)));

    restClient.post()
        .uri(REGISTER_PIN_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT, TEST_CLIENT_ID))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            { "jwt": "%s" }
            """.formatted(TEST_JWT));
  }

  @Test
  void changesPin() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/operations")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "text/plain")
            .withBody(TEST_JWT)));

    restClient.put()
        .uri(CHANGE_PIN_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT, TEST_CLIENT_ID))
        .exchange()
        .expectStatus()
        .isEqualTo(200)
        .expectBody()
        .json("""
            { "jwt": "%s" }
            """.formatted(TEST_JWT));
  }

  @Test
  void createsSecureSession() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/operations")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "text/plain")
            .withBody(TEST_JWT)));

    restClient.post()
        .uri(CREATE_SESSION_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT, TEST_CLIENT_ID))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            { "jwt": "%s" }
            """.formatted(TEST_JWT));
  }

  @Test
  void createsKey() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/operations")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "text/plain")
            .withBody(TEST_JWT)));

    restClient.post()
        .uri(CREATE_KEY_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT, TEST_CLIENT_ID))
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            { "jwt": "%s" }
            """.formatted(TEST_JWT));
  }

  @Test
  void listsKeys() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/operations")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "text/plain")
            .withBody(TEST_JWT)));

    restClient.post()
        .uri(LIST_KEYS_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT, TEST_CLIENT_ID))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json("""
            { "jwt": "%s" }
            """.formatted(TEST_JWT));
  }

  @Test
  void deletesKey() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/operations")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "text/plain")
            .withBody(TEST_JWT)));

    restClient.post()
        .uri(DELETE_KEY_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT, TEST_CLIENT_ID))
        .exchange()
        .expectStatus()
        .isNoContent();
  }

  @Test
  void signsPayload() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/operations")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "text/plain")
            .withBody(TEST_JWT)));

    restClient.post()
        .uri(SIGN_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT, TEST_CLIENT_ID))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json("""
            { "jwt": "%s" }
            """.formatted(TEST_JWT));
  }

  @Test
  void registerPinAsyncAcceptsPendingResponse() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/requests")
        .withRequestBody(WireMock.matchingJsonPath("$.outerRequestJws",
            WireMock.equalTo(TEST_JWT)))
        .withRequestBody(WireMock.matchingJsonPath("$.clientId",
            WireMock.equalTo(TEST_CLIENT_ID)))
        .willReturn(aResponse()
            .withStatus(202)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "correlationId": "%s",
                  "status": "pending",
                  "resultUrl": "%s%s"
                }
                """.formatted(TEST_CORRELATION_ID, BFF_RESULT_URL, TEST_CORRELATION_ID))));

    restClient.post()
        .uri(REGISTER_PIN_ASYNC_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT, TEST_CLIENT_ID))
        .exchange()
        .expectStatus()
        .isAccepted()
        .expectBody()
        .json("""
            {
              "correlationId": "%s",
              "status": "pending",
              "resultUrl": "http://localhost:%d/v0/hsm/requests/%s"
            }
            """.formatted(TEST_CORRELATION_ID, port, TEST_CORRELATION_ID));
  }

  @Test
  void signAsyncAcceptsCompletedResponse() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/requests")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "correlationId": "%s",
                  "status": "complete",
                  "result": "%s"
                }
                """.formatted(TEST_CORRELATION_ID, TEST_JWT))));

    restClient.post()
        .uri(SIGN_ASYNC_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT, TEST_CLIENT_ID))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json("""
            {
              "correlationId": "%s",
              "status": "complete",
              "result": "%s"
            }
            """.formatted(TEST_CORRELATION_ID, TEST_JWT));
  }

  @Test
  void listKeysAsyncSendsOuterRequestJwsToBff() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/requests")
        .withRequestBody(WireMock.matchingJsonPath("$.outerRequestJws",
            WireMock.equalTo(TEST_JWT)))
        .withRequestBody(WireMock.matchingJsonPath("$.clientId",
            WireMock.equalTo(TEST_CLIENT_ID)))
        .willReturn(aResponse()
            .withStatus(202)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "correlationId": "%s",
                  "status": "pending",
                  "resultUrl": "%s%s"
                }
                """.formatted(TEST_CORRELATION_ID, BFF_RESULT_URL, TEST_CORRELATION_ID))));

    restClient.post()
        .uri(LIST_KEYS_ASYNC_URL)
        .header("content-type", "application/json")
        .body(new HsmRequestDto(TEST_JWT, TEST_CLIENT_ID))
        .exchange()
        .expectStatus()
        .isAccepted()
        .expectBody()
        .json("""
            {
              "correlationId": "%s",
              "status": "pending",
              "resultUrl": "http://localhost:%d/v0/hsm/requests/%s"
            }
            """.formatted(TEST_CORRELATION_ID, port, TEST_CORRELATION_ID));
  }

  @Test
  void pollingAcceptsPendingResponse() {
    r2psServer.stubFor(WireMock.get("/hsm/v1/requests/" + TEST_CORRELATION_ID)
        .willReturn(aResponse()
            .withStatus(202)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "correlationId": "%s",
                  "status": "pending",
                  "resultUrl": "%s%s"
                }
                """.formatted(TEST_CORRELATION_ID, BFF_RESULT_URL, TEST_CORRELATION_ID))));

    restClient.get()
        .uri(HSM_REQUESTS_URL + "/" + TEST_CORRELATION_ID)
        .exchange()
        .expectStatus()
        .isAccepted()
        .expectBody()
        .json("""
            {
              "correlationId": "%s",
              "status": "pending",
              "resultUrl": "http://localhost:%d/v0/hsm/requests/%s"
            }
            """.formatted(TEST_CORRELATION_ID, port, TEST_CORRELATION_ID));
  }

  @Test
  void pollingAcceptsCompletedResponse() {
    r2psServer.stubFor(WireMock.get("/hsm/v1/requests/" + TEST_CORRELATION_ID)
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "correlationId": "%s",
                  "status": "complete",
                  "result": "%s"
                }
                """.formatted(TEST_CORRELATION_ID, TEST_JWT))));

    restClient.get()
        .uri(HSM_REQUESTS_URL + "/" + TEST_CORRELATION_ID)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json("""
            {
              "correlationId": "%s",
              "status": "complete",
              "result": "%s"
            }
            """.formatted(TEST_CORRELATION_ID, TEST_JWT));
  }

  @Test
  void rejectsUnauthenticatedDeviceStateRegistration() {
    RestTestClient unauthenticated = RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build();

    unauthenticated.post()
        .uri(REGISTER_STATE_URL)
        .header("content-type", "application/json")
        .body(se.digg.wallet.gateway.api.v0.model.RegisterStateRequestDto.builder()
            .publicKey(se.digg.wallet.gateway.api.v0.model.EcPublicJwkDto.builder()
                .kty("EC")
                .crv("P-256")
                .x("x")
                .y("y")
                .kid("kid")
                .build())
            .overwrite(false)
            .ttl("30d")
            .build())
        .exchange()
        .expectStatus()
        .isForbidden();
  }

}
