// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static se.digg.wallet.gateway.application.controller.util.DpopTokens.accessToken;
import static se.digg.wallet.gateway.application.controller.util.DpopTokens.newKey;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.jwk.ECKey;
import com.redis.testcontainers.RedisContainer;
import java.util.UUID;
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
import se.digg.wallet.gateway.api.v0.model.CreateAccountRequest;
import se.digg.wallet.gateway.api.v0.model.CreateAccountResponse;
import se.digg.wallet.gateway.api.v0.model.EcJwkRequest;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.AuthorizationServerMock;
import se.digg.wallet.gateway.application.controller.util.DpopTokens;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;

/**
 * The app puts its wallet under the account in its access token, with the device key the token is
 * bound to.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri="
            + "http://localhost:${wiremock.authorization-server.port}/oauth2/jwks",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + DpopTokens.ISSUER,
        "spring.security.oauth2.resourceserver.jwt.audiences=" + DpopTokens.AUDIENCE,
        "spring.security.oauth2.resourceserver.jwt.jws-algorithms=ES256"})
@AuthorizationServerMock
@WalletAccountMock
@Testcontainers
@ActiveProfiles("test")
class PutWalletIntegrationTest {

  private static final String PATH = "/v0/wallets";
  private static final String API_KEY = "my_secret_key";
  private static final ECKey SIGNING_KEY = newKey("as-key");

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  @LocalServerPort
  private int port;

  @InjectWireMock(AuthorizationServerMock.NAME)
  private WireMockServer authorizationServer;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer accountServer;

  private RestTestClient client;
  private final String accountId = UUID.randomUUID().toString();
  private final ECKey deviceKey = newKey(UUID.randomUUID().toString());

  @BeforeEach
  void setUp() {
    DpopTokens.stubJwks(authorizationServer, SIGNING_KEY);
    client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @Test
  void a_new_wallet_is_stored_under_the_account_in_the_token() {
    stubWalletAccount(201);

    var response = putWallet(deviceKey)
        .expectStatus().isCreated()
        .expectBody(CreateAccountResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(response.getAccountId()).hasToString(accountId);
    accountServer.verify(putRequestedFor(urlPathMatching("/v0/accounts/" + accountId + "/wallets"))
        .withRequestBody(equalToJson("""
            {"deviceKey": {"kid": "%s", "x": "%s", "y": "%s"}}
            """.formatted(deviceKey.getKeyID(), deviceKey.getX(), deviceKey.getY()), true, true)));
  }

  @Test
  void a_stored_wallet_is_returned_as_ok() {
    stubWalletAccount(200);

    putWallet(deviceKey).expectStatus().isOk();
  }

  @Test
  void a_wallet_conflict_in_wallet_account_is_a_conflict() {
    accountServer.stubFor(put(urlPathMatching("/v0/accounts/.*/wallets"))
        .willReturn(aResponse().withStatus(409)));

    putWallet(deviceKey).expectStatus().isEqualTo(409);
  }

  @Test
  void a_device_key_other_than_the_bound_key_is_a_bad_request() {
    String token = accessToken(SIGNING_KEY, accountId, deviceKey);

    client.put().uri(PATH)
        .header("Authorization", "DPoP " + token)
        .header("DPoP", proof(token))
        .header(SecurityConfig.API_KEY_HEADER, API_KEY)
        .body(request(newKey("other-key")))
        .exchange()
        .expectStatus().isBadRequest();
    assertThat(accountServer.getAllServeEvents()).isEmpty();
  }

  @Test
  void a_device_key_that_is_not_a_point_on_the_curve_is_a_bad_request() {
    String token = accessToken(SIGNING_KEY, accountId, deviceKey);
    var notOnCurve = CreateAccountRequest.builder()
        .deviceKey(EcJwkRequest.builder()
            .kty("EC")
            .crv("P-256")
            .kid(deviceKey.getKeyID())
            .x(deviceKey.getX().toString())
            .y(deviceKey.getX().toString())
            .build())
        .build();

    client.put().uri(PATH)
        .header("Authorization", "DPoP " + token)
        .header("DPoP", proof(token))
        .header(SecurityConfig.API_KEY_HEADER, API_KEY)
        .body(notOnCurve)
        .exchange()
        .expectStatus().isBadRequest();
    assertThat(accountServer.getAllServeEvents()).isEmpty();
  }

  @Test
  void a_challenge_response_session_cannot_put_a_wallet() throws Exception {
    AuthUtil.login(accountServer, port, client).put().uri(PATH)
        .body(request(deviceKey))
        .exchange()
        .expectStatus().isForbidden();
  }

  private RestTestClient.ResponseSpec putWallet(ECKey key) {
    String token = accessToken(SIGNING_KEY, accountId, deviceKey);
    return client.put().uri(PATH)
        .header("Authorization", "DPoP " + token)
        .header("DPoP", proof(token))
        .header(SecurityConfig.API_KEY_HEADER, API_KEY)
        .body(request(key))
        .exchange();
  }

  private String proof(String token) {
    return DpopTokens.proof(deviceKey, "PUT", "http://localhost:" + port + PATH, token);
  }

  private void stubWalletAccount(int status) {
    accountServer.stubFor(put(urlPathMatching("/v0/accounts/" + accountId + "/wallets"))
        .willReturn(aResponse()
            .withStatus(status)
            .withHeader("content-type", "application/json")
            .withBody("""
                {"id": "%s", "deviceKey": {"kty": "EC", "kid": "%s", "crv": "P-256",
                 "x": "%s", "y": "%s"}}
                """.formatted(accountId, deviceKey.getKeyID(), deviceKey.getX(),
                deviceKey.getY()))));
  }

  private static CreateAccountRequest request(ECKey key) {
    return CreateAccountRequest.builder()
        .deviceKey(EcJwkRequest.builder()
            .kty("EC")
            .crv("P-256")
            .kid(key.getKeyID())
            .x(key.getX().toString())
            .y(key.getY().toString())
            .build())
        .build();
  }
}
