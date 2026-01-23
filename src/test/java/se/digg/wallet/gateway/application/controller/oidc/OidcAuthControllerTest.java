// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.oidc;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.AuthorizationServerMock;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@AuthorizationServerMock
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureRestTestClient
class OidcAuthControllerTest {

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  @Autowired
  private RestTestClient restClient;

  @LocalServerPort
  private int port;

  @InjectWireMock(AuthorizationServerMock.NAME)
  private WireMockServer authorizationServer;

  private boolean authenticated;

  @BeforeEach
  public void beforeEach() throws Exception {
    if (!authenticated) {
      restClient =
          restClient
              .mutate()
              .defaultHeader("SESSION", AuthUtil.oauth2Login(port, authorizationServer))
              .build();
      authenticated = true;
    }
  }

  @Test
  void testGetSessionId() throws Exception {
    restClient
        .get()
        .uri("/oidc/auth")
        .exchange()
        .expectStatus()
        .is2xxSuccessful();
  }
}
