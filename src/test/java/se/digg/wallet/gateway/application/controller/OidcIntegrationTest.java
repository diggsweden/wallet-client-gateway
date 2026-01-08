// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.redis.testcontainers.RedisContainer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.AuthorizationServerMock;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AuthorizationServerMock
@Testcontainers
@ActiveProfiles("test")
class OidcIntegrationTest {

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  @InjectWireMock(AuthorizationServerMock.NAME)
  private WireMockServer authorizationServer;

  private HttpClient httpClient;

  @LocalServerPort
  private int port;

  private String testGetEndpoint;
  private String testLogoutEndpoint;

  @BeforeEach
  void beforeEach() throws Exception {
    this.httpClient = HttpClient.newHttpClient();
    this.testGetEndpoint = "http://localhost:%s/oidc/session/test".formatted(port);
    this.testLogoutEndpoint = "http://localhost:%s/oidc/session/logout".formatted(port);
  }

  @Test
  void cannotUseInvalidSession() throws Exception {
    var response = httpClient.send(
        HttpRequest.newBuilder().GET()
            .uri(URI.create(testGetEndpoint))
            .header("SESSION", "nonexistant")
            .build(),
        BodyHandlers.discarding());
    assertThat(response.statusCode()).isEqualTo(302);
  }

  @Test
  void withoutSessionRedirects() throws Exception {
    var response = httpClient.send(
        HttpRequest.newBuilder().GET()
            .uri(URI.create(testGetEndpoint))
            .build(),
        BodyHandlers.discarding());
    assertThat(response.statusCode()).isEqualTo(302);
  }

  @Test
  void happyPathLogin() throws Exception {
    var step1Result = AuthUtil.step1CallProtectedEndpoint(port);
    var loginSession = step1Result.sessionId().orElseThrow();
    var step2Result =
        AuthUtil.step2CallSpringLoginEndpoint(step1Result.location(), loginSession);
    var step3Result = AuthUtil.step3BuildFakedAsAuthResult(port, step2Result.location());
    AuthUtil.step4StubAsyncAuthorizationServerCalls(step3Result.code(), step3Result.nonce(),
        authorizationServer);
    var step5Result =
        AuthUtil.step5CallLoginEndpointWithCode(step3Result.location(), loginSession);

    var session = step5Result.response().headers().firstValue("SESSION").orElseThrow();
    var response = httpClient.send(
        HttpRequest.newBuilder(URI.create(testGetEndpoint))
            .header("SESSION", session)
            .build(),
        BodyHandlers.discarding());
    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  void stateIsValidated() throws Exception {
    var step1Result = AuthUtil.step1CallProtectedEndpoint(port);
    var loginSession = step1Result.sessionId().orElseThrow();
    var step2Result =
        AuthUtil.step2CallSpringLoginEndpoint(step1Result.location(), loginSession);

    var locationWithWrongState = step2Result.location()
        .replaceFirst("(state=.+?)(&|$)", "state=bogus-state$2");
    var step3Result = AuthUtil.step3BuildFakedAsAuthResult(port, locationWithWrongState);
    // skip step4
    var step5Result =
        AuthUtil.step5CallLoginEndpointWithCode(step3Result.location(), loginSession);
    assertThat(step5Result.response().statusCode())
        .isEqualTo(302);
    assertThat(step5Result.response().headers().firstValue("location"))
        .isPresent()
        .isEqualTo(Optional.of("http://localhost:%s/login?error".formatted(port)));
  }

  @Test
  void authorizationServerTokenEndpoint401() throws Exception {
    var step1Result = AuthUtil.step1CallProtectedEndpoint(port);
    var loginSession = step1Result.sessionId().orElseThrow();
    var step2Result =
        AuthUtil.step2CallSpringLoginEndpoint(step1Result.location(), loginSession);
    var step3Result = AuthUtil.step3BuildFakedAsAuthResult(port, step2Result.location());
    AuthUtil.step4StubAsyncAuthorizationServerCalls(step3Result.code(), step3Result.nonce(),
        authorizationServer);
    // override /token
    authorizationServer.stubFor(post(urlEqualTo("/token"))
        .withRequestBody(containing("code=" + step3Result.code()))
        .atPriority(1)
        .willReturn(aResponse().withStatus(401)));
    var step5Result =
        AuthUtil.step5CallLoginEndpointWithCode(step3Result.location(), loginSession);
    assertThat(step5Result.response().statusCode()).isEqualTo(403);
  }

  @Test
  void cannotUseInvalidatedSession() throws Exception {
    var session = AuthUtil.oauth2Login(port, authorizationServer);
    var getResponse1 = httpClient.send(
        HttpRequest.newBuilder(URI.create(testGetEndpoint))
            .header("SESSION", session)
            .build(),
        BodyHandlers.discarding());
    assertThat(getResponse1.statusCode()).isEqualTo(200);

    var logoutResponse = httpClient.send(
        HttpRequest.newBuilder(URI.create(testLogoutEndpoint))
            .header("SESSION", session)
            .build(),
        BodyHandlers.discarding());
    assertThat(logoutResponse.statusCode()).isEqualTo(200);

    var getResponse2 = httpClient.send(
        HttpRequest.newBuilder(URI.create(testGetEndpoint))
            .header("SESSION", session)
            .build(),
        BodyHandlers.discarding());
    assertThat(getResponse2.statusCode()).isEqualTo(302);
  }

  @Test
  void cannotUseSessionForChallengeResponseEndpoint() throws Exception {
    var session = AuthUtil.oauth2Login(port, authorizationServer);
    var response = httpClient.send(
        HttpRequest
            .newBuilder(URI.create(
                "http://localhost:%s/private/user/session/test".formatted(port)))
            .header("SESSION", session)
            .build(),
        BodyHandlers.discarding());
    assertThat(response.statusCode()).isEqualTo(403);
  }

  @Test
  void pkceIsEnabled() throws Exception {
    var step1Result = AuthUtil.step1CallProtectedEndpoint(port);
    var loginSession = step1Result.sessionId().orElseThrow();
    var step2Result =
        AuthUtil.step2CallSpringLoginEndpoint(step1Result.location(), loginSession);


    assertThat(step2Result.location())
        .contains(
            "code_challenge=",
            "code_challenge_method=S256",
            "response_type=code");
  }
}
