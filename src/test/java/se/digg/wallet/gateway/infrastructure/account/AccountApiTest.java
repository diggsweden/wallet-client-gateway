// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.account;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.client.account.v0.api.AccountApi;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@ActiveProfiles("test")
@AutoConfigureRestTestClient
public class AccountApiTest {

  @Autowired
  private AccountApi accountApiClient;

  @Value("${wiremock.server.baseUrl}")
  private String wireMockUrl;

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer server;

  @Test
  void clientContext_autowired_basePathStartsWithHttp() {
    assertThat(accountApiClient).isNotNull();
    var basePath = accountApiClient.getApiClient().getBasePath();
    assertThat(basePath).startsWith("http");
  }
}
