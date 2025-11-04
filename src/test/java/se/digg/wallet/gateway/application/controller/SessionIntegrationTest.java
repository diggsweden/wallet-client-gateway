// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import se.digg.wallet.gateway.application.controller.SessionTestController.SessionTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(port = 0))
class SessionIntegrationTest {

  @Autowired
  private WebTestClient restClient;

  private boolean authenticated = false;

  @LocalServerPort
  private int port;

  @BeforeEach
  void beforeEach() throws Exception {
    if (!authenticated) {
      restClient = AuthUtil.login(port, restClient);
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
        .cookie("JSESSIONID", "10238128301")
        .exchange()
        .expectStatus()
        .isEqualTo(403);
  }

  @Test
  void emptySessionIdIsForbidden() {
    restClient.get()
        .uri("/private/user/session/test")
        .cookie("JSESSIONID", "")
        .exchange()
        .expectStatus()
        .isEqualTo(403);
  }

  @SuppressWarnings("null")
  @Test
  void noSessionIdIsForbidden() {
    restClient.get()
        .uri("/private/user/session/test")
        .cookie("JSESSIONID", null)
        .exchange()
        .expectStatus()
        .isEqualTo(403);
  }


  @Test
  void canGenerateAnotherSessionWhenLoggedInAndBothAreValid() throws Exception {
    var latestRestClient = AuthUtil.login(port, restClient);
    // TODO not a good thing?
    latestRestClient.get()
        .uri("/private/user/session/test")
        .exchange()
        .expectStatus()
        .isEqualTo(200);

    restClient.get()
        .uri("/private/user/session/test")
        .exchange()
        .expectStatus()
        .isEqualTo(200);
  }

  @Test
  void cannotUseInvalidatedSession() throws Exception {
    var loggingOutRestClient = AuthUtil.login(port, restClient);
    loggingOutRestClient.get()
        .uri("/private/user/session/logout")
        .exchange()
        .expectStatus()
        .isEqualTo(200);

    loggingOutRestClient.get()
        .uri("/private/user/session/test")
        .exchange()
        .expectStatus()
        .isEqualTo(403);
  }
}
