// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.redis.testcontainers.RedisContainer;
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
import se.digg.wallet.gateway.application.config.SessionConfig;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.controller.util.ChallengeResponseSessionTestController.SessionTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@Testcontainers
@ActiveProfiles("test")
class ChallengeResponseSessionIntegrationTest {

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  private static RestTestClient restClient;

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer accountServer;


  @BeforeEach
  void beforeEach() throws Exception {
    restClient = RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build();
  }

  @Test
  void accountIdIsReturned() throws Exception {
    var authenticatedRestClient = AuthUtil.login(accountServer, port, restClient);
    authenticatedRestClient.get()
        .uri("/private/user/session/test")
        .exchange()
        .expectStatus()
        .isEqualTo(200)
        .expectBody(SessionTest.class)
        .value(response -> assertThat(response.accountId()).isEqualTo(AuthUtil.ACCOUNT_ID));
  }

  @Test
  void invalidSessionIdIsForbidden() {
    restClient.get()
        .uri("/private/user/session/test")
        .header(SessionConfig.SESSION_HEADER, "10238128301")
        .exchange()
        .expectStatus()
        .isEqualTo(403);
  }

  @Test
  void emptySessionIdIsForbidden() {
    restClient.get()
        .uri("/private/user/session/test")
        .header(SessionConfig.SESSION_HEADER, "")
        .exchange()
        .expectStatus()
        .isEqualTo(403);
  }

  @Test
  void noSessionIdIsForbidden() {
    restClient.get()
        .uri("/private/user/session/test")
        .exchange()
        .expectStatus()
        .isEqualTo(403);
  }

  @Test
  void cannotUseInvalidatedSession() throws Exception {
    var authenticatedRestClient = AuthUtil.login(accountServer, port, restClient);
    authenticatedRestClient.get()
        .uri("/private/user/session/logout")
        .exchange()
        .expectStatus()
        .isEqualTo(200);

    authenticatedRestClient.get()
        .uri("/private/user/session/test")
        .exchange()
        .expectStatus()
        .isEqualTo(403);
  }
}
