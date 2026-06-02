// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.api.v0.model.WuaDto;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.controller.util.WalletProviderMock;
import se.digg.wallet.gateway.application.model.KeyRequestTestBuilder;
import se.digg.wallet.gateway.application.model.common.JwkDto;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@WalletProviderMock
@Testcontainers
@ActiveProfiles("test")
class WuaControllerIntegrationTest {

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();
  private static JwkDto jwkDto;
  public static String TEST_JWK_STRING;

  public static final UUID TEST_WALLET_ID = UUID.randomUUID();
  public static final String TEST_NONCE = "nonce";
  /* # nosemgrep */
  private static final String SIGNED_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
      + "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlF1aW5jeSBMYXJzb24i"
      + "LCJpYXQiOjE1MTYyMzkwMjJ9."
      + "WcPGXClpKD7Bc1C0CCDA1060E2GGlTfamrd8-W0ghBE";
  private RestTestClient restClient;

  private static final String WUA_URL = "/wallet-provider/wallet-unit-attestation";

  private boolean authenticated = false;
  private static final String ACCOUNT_ID = UUID.randomUUID().toString();
  private static ECKey generatedKeyPair;

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer accountServer;

  @InjectWireMock(WalletProviderMock.NAME)
  private WireMockServer providerServer;

  @BeforeAll
  public static void beforeAll() throws Exception {
    generatedKeyPair = AuthUtil.generateKey();
    jwkDto = KeyRequestTestBuilder.ofLegacy(generatedKeyPair).build();
    TEST_JWK_STRING =
        new ObjectMapper().writeValueAsString(
            new ObjectMapper().writeValueAsString(jwkDto));
    // remove wrapped outer quotes
    TEST_JWK_STRING = TEST_JWK_STRING.substring(1, TEST_JWK_STRING.length() - 1);
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
  void testRequestingWuaNotAuthenticatedReturnsBadRequest() {
    authenticated = false;

    RestTestClient unauthenticatedClient = RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build();

    var response = unauthenticatedClient.post()
        .uri("/wua" + "?nonce=" + TEST_NONCE)
        .exchange();

    response.expectStatus()
        .isBadRequest();
  }

  @Test
  void testRequestingWuaNoAccountIdReturnsUnAuthorized() {
    AuthUtil.ACCOUNT_ID = null;

    // Create a new RestTestClient without authentication (no session header)
    RestTestClient unauthenticatedClient = RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build();

    var response = unauthenticatedClient.post()
        .uri("/wua" + "?nonce=" + TEST_NONCE)
        .exchange();

    // Spring Security's challengeResponseAuthorizationMgr() blocks requests that don't have
    // a valid ChallengeResponseAuthentication in the session, returning 403 Forbidden
    response.expectStatus()
        .isBadRequest();

    // Revert for other tests using AuthUtil.ACCOUNT_ID.
    AuthUtil.ACCOUNT_ID = ACCOUNT_ID;
  }

  @Test
  void testRequestingWuaSuccessfullyReturnsCreated() {
    providerServer.stubFor(post(WUA_URL)
        .withRequestBody(equalToJson("""
            {
              "jwk": "%s",
              "nonce": "%s"
            }
            """.formatted(TEST_JWK_STRING, TEST_NONCE)))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "text/plain")
            .withBody(SIGNED_JWT)));

    var response = restClient.post()
        .uri("/wua" + "?nonce=" + TEST_NONCE)
        .exchange();

    response.expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            {
              "jwt": "%s"
            }
            """.formatted(SIGNED_JWT));
  }

  @Test
  void testRequestingWuaFailsReturnsInternalServerError() {
    providerServer.stubFor(post(WUA_URL)
        .withRequestBody(equalToJson("""
            {
              "jwk": "%s",
              "nonce": ""
            }
            """.formatted(TEST_JWK_STRING)))
        .willReturn(aResponse()
            .withStatus(400)));

    var response = restClient.post()
        .uri("/wua")
        .contentType(MediaType.APPLICATION_JSON)
        .body(WuaDto.builder()
            .jwt(TEST_JWK_STRING)
            .build())
        .exchange();

    response.expectStatus()
        .isEqualTo(500);
  }

  @Test
  void testValidation_emptyNonce() {
    providerServer.stubFor(post(WUA_URL)
        .withRequestBody(equalToJson("""
            {
              "jwk": "%s",
              "nonce": "%s"
            }
            """.formatted(TEST_JWK_STRING, "")))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "text/plain")
            .withBody(SIGNED_JWT)));

    // Sending empty nonce should be fail
    var response = restClient.post()
        .uri("/wua" + "?nonce=")
        .exchange();

    response.expectStatus()
        .isEqualTo(400);
  }

  @Test
  void testValidation_nullNonce() {
    providerServer.stubFor(post(WUA_URL)
        .withRequestBody(equalToJson("""
            {
              "jwk": "%s",
              "nonce": "%s"
            }
            """.formatted(TEST_JWK_STRING, null)))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "text/plain")
            .withBody(SIGNED_JWT)));

    // Sending null nonce should be accepted
    var response = restClient.post()
        .uri("/wua" + "?nonce=" + null)
        .exchange();

    response.expectStatus()
        .isEqualTo(201);
  }

  @Test
  void testValidation_withoutNonce() {
    providerServer.stubFor(post(WUA_URL)
        .withRequestBody(equalToJson("""
            {
              "jwk": "%s",
              "nonce": ""
            }
            """.formatted(TEST_JWK_STRING)))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "text/plain")
            .withBody(SIGNED_JWT)));

    // Not sending nonce should be accepted
    var response = restClient.post()
        .uri("/wua")
        .exchange();

    response.expectStatus()
        .isEqualTo(201);
  }
}
