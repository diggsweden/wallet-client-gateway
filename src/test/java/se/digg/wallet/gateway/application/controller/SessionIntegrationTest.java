// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import se.digg.wallet.gateway.application.config.SessionConfig;
import se.digg.wallet.gateway.application.controller.SessionTestController.SessionTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(port = 0))
@Testcontainers
class SessionIntegrationTest {

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  @Autowired
  private WebTestClient rawRestClient;

  private WebTestClient restClient;

  private boolean authenticated = false;

  @LocalServerPort
  private int port;

  @BeforeEach
  void beforeEach() throws Exception {
    if (!authenticated) {
      restClient = AuthUtil.login(port, rawRestClient);
      authenticated = true;
    }
  }

  @Test
  void accountIdIsReturned() {
    restClient.get()
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

  @SuppressWarnings("null")
  @Test
  void noSessionIdIsForbidden() {
    rawRestClient.get()
        .uri("/private/user/session/test")
        .exchange()
        .expectStatus()
        .isEqualTo(403);
  }

  @Test
  @Order(99)
  void cannotUseInvalidatedSession() throws Exception {
    restClient.get()
        .uri("/private/user/session/logout")
        .exchange()
        .expectStatus()
        .isEqualTo(200);

    restClient.get()
        .uri("/private/user/session/test")
        .exchange()
        .expectStatus()
        .isEqualTo(403);
  }
}
