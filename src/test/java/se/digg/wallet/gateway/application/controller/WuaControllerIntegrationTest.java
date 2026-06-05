// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
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
import se.digg.wallet.gateway.domain.model.account.Jwk;
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
  public static String WALLET_JWK_STRING;

  public static final UUID TEST_WALLET_ID = UUID.randomUUID();
  public static final String TEST_NONCE = "nonce";
  /* # nosemgrep */
  private static final String SIGNED_JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
      + "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlF1aW5jeSBMYXJzb24i"
      + "LCJpYXQiOjE1MTYyMzkwMjJ9."
      + "WcPGXClpKD7Bc1C0CCDA1060E2GGlTfamrd8-W0ghBE";
  private RestTestClient restClient;

  private static final String WUA_URL = "/wallet-provider/wallet-unit-attestation";
  private static final String WALLET_KEYS_URL = "/v0/accounts/";

  private boolean authenticated = false;
  private static final String ACCOUNT_ID = UUID.randomUUID().toString();
  private static ECKey generatedKeyPair;
  private static ECKey walletKeyPair;

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer accountServer;

  @InjectWireMock(WalletProviderMock.NAME)
  private WireMockServer providerServer;

  @BeforeAll
  public static void beforeAll() throws Exception {
    generatedKeyPair = AuthUtil.generateKey();
    walletKeyPair = AuthUtil.generateKey();

    var walletJwk = new Jwk(
        walletKeyPair.getKeyType().getValue(),
        walletKeyPair.getKeyID(),
        walletKeyPair.getAlgorithm().toString(),
        walletKeyPair.getKeyUse().getValue(),
        walletKeyPair.getCurve().toString(),
        walletKeyPair.getX().toString(),
        walletKeyPair.getY().toString());

    var mapper = new ObjectMapper();
    WALLET_JWK_STRING = mapper.writeValueAsString(mapper.writeValueAsString(walletJwk));
    // remove wrapped outer quotes
    WALLET_JWK_STRING = WALLET_JWK_STRING.substring(1, WALLET_JWK_STRING.length() - 1);
  }

  @BeforeEach
  public void beforeEach() throws Exception {
    accountServer.stubFor(get(WALLET_KEYS_URL + ACCOUNT_ID + "/wallet-keys")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "items": [
                    {
                      "kid": "%s",
                      "kty": "%s",
                      "alg": "%s",
                      "use": "%s",
                      "crv": "%s",
                      "x": "%s",
                      "y": "%s"
                    }
                  ]
                }
                """.formatted(
                walletKeyPair.getKeyID(),
                walletKeyPair.getKeyType().getValue(),
                walletKeyPair.getAlgorithm().toString(),
                walletKeyPair.getKeyUse().getValue(),
                walletKeyPair.getCurve().toString(),
                walletKeyPair.getX().toString(),
                walletKeyPair.getY().toString()))));

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
            """.formatted(WALLET_JWK_STRING, TEST_NONCE)))
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
            """.formatted(WALLET_JWK_STRING)))
        .willReturn(aResponse()
            .withStatus(400)));

    var response = restClient.post()
        .uri("/wua")
        .contentType(MediaType.APPLICATION_JSON)
        .body(WuaDto.builder()
            .jwt(WALLET_JWK_STRING)
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
            """.formatted(WALLET_JWK_STRING, "")))
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
            """.formatted(WALLET_JWK_STRING, null)))
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
            """.formatted(WALLET_JWK_STRING)))
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
