// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

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
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.AuthorizationServerMock;
import se.digg.wallet.gateway.application.controller.util.ChallengeResponseSessionTestController.SessionTest;
import se.digg.wallet.gateway.application.controller.util.DpopTokens;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;

/**
 * A DPoP-bound access token from the authorization server authenticates the app, as an alternative
 * to the challenge-response session.
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
class DpopTokenAuthenticationIntegrationTest {

  private static final String PATH = "/private/user/session/test";
  private static final String API_KEY = "my_secret_key";
  private static final String ACCOUNT_ID = UUID.randomUUID().toString();
  private static final ECKey SIGNING_KEY = newKey("as-key");
  private static final ECKey DEVICE_KEY = newKey("device-key");

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

  @BeforeEach
  void setUp() {
    DpopTokens.stubJwks(authorizationServer, SIGNING_KEY);
    client = RestTestClient.bindToServer().baseUrl(url()).build();
  }

  @Test
  void a_dpop_bound_token_with_a_proof_authenticates_the_account_in_sub() {
    String token = accessToken(SIGNING_KEY, ACCOUNT_ID, DEVICE_KEY);

    client.get().uri(PATH)
        .header("Authorization", "DPoP " + token)
        .header("DPoP", proof(DEVICE_KEY, token))
        .header(SecurityConfig.API_KEY_HEADER, API_KEY)
        .exchange()
        .expectStatus().isOk()
        .expectBody(SessionTest.class)
        .value(response -> assertThat(response.accountId()).isEqualTo(ACCOUNT_ID));
  }

  @Test
  void a_dpop_bound_token_is_not_accepted_as_a_bearer_token() {
    client.get().uri(PATH)
        .header("Authorization", "Bearer " + accessToken(SIGNING_KEY, ACCOUNT_ID, DEVICE_KEY))
        .header(SecurityConfig.API_KEY_HEADER, API_KEY)
        .exchange()
        .expectStatus().isUnauthorized();
  }

  @Test
  void a_proof_signed_by_another_key_is_rejected() {
    String token = accessToken(SIGNING_KEY, ACCOUNT_ID, DEVICE_KEY);

    client.get().uri(PATH)
        .header("Authorization", "DPoP " + token)
        .header("DPoP", proof(newKey("other-key"), token))
        .header(SecurityConfig.API_KEY_HEADER, API_KEY)
        .exchange()
        .expectStatus().isUnauthorized();
  }

  @Test
  void a_proof_for_another_token_is_rejected() {
    String token = accessToken(SIGNING_KEY, ACCOUNT_ID, DEVICE_KEY);
    String otherToken = accessToken(SIGNING_KEY, ACCOUNT_ID, DEVICE_KEY);

    client.get().uri(PATH)
        .header("Authorization", "DPoP " + token)
        .header("DPoP", proof(DEVICE_KEY, otherToken))
        .header(SecurityConfig.API_KEY_HEADER, API_KEY)
        .exchange()
        .expectStatus().isUnauthorized();
  }

  @Test
  void a_token_not_signed_by_the_authorization_server_is_rejected() {
    String token = accessToken(newKey("as-key"), ACCOUNT_ID, DEVICE_KEY);

    client.get().uri(PATH)
        .header("Authorization", "DPoP " + token)
        .header("DPoP", proof(DEVICE_KEY, token))
        .header(SecurityConfig.API_KEY_HEADER, API_KEY)
        .exchange()
        .expectStatus().isUnauthorized();
  }

  @Test
  void a_token_from_another_issuer_is_rejected() {
    String token = accessToken(SIGNING_KEY, "https://other-issuer.test", ACCOUNT_ID, DEVICE_KEY);

    client.get().uri(PATH)
        .header("Authorization", "DPoP " + token)
        .header("DPoP", proof(DEVICE_KEY, token))
        .header(SecurityConfig.API_KEY_HEADER, API_KEY)
        .exchange()
        .expectStatus().isUnauthorized()
        .expectHeader().value("WWW-Authenticate",
            value -> assertThat(value).startsWith("DPoP").contains("invalid_token"));
  }

  @Test
  void a_token_for_another_audience_is_rejected() {
    String token = accessToken(SIGNING_KEY, DpopTokens.ISSUER, "other-client", ACCOUNT_ID,
        DEVICE_KEY);

    client.get().uri(PATH)
        .header("Authorization", "DPoP " + token)
        .header("DPoP", proof(DEVICE_KEY, token))
        .header(SecurityConfig.API_KEY_HEADER, API_KEY)
        .exchange()
        .expectStatus().isUnauthorized()
        .expectHeader().value("WWW-Authenticate",
            value -> assertThat(value).startsWith("DPoP").contains("invalid_token"));
  }

  @Test
  void the_challenge_response_session_still_authenticates() throws Exception {
    AuthUtil.login(accountServer, port, client).get().uri(PATH)
        .exchange()
        .expectStatus().isOk()
        .expectBody(SessionTest.class)
        .value(response -> assertThat(response.accountId()).isEqualTo(AuthUtil.ACCOUNT_ID));
  }

  @Test
  void a_request_without_session_or_token_is_still_forbidden() {
    client.get().uri(PATH)
        .header(SecurityConfig.API_KEY_HEADER, API_KEY)
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  void a_valid_token_still_needs_the_api_key() {
    String token = accessToken(SIGNING_KEY, ACCOUNT_ID, DEVICE_KEY);

    client.get().uri(PATH)
        .header("Authorization", "DPoP " + token)
        .header("DPoP", proof(DEVICE_KEY, token))
        .exchange()
        .expectStatus().isForbidden();
  }

  private String url() {
    return "http://localhost:" + port;
  }

  private String proof(ECKey key, String token) {
    return DpopTokens.proof(key, "GET", url() + PATH, token);
  }
}
