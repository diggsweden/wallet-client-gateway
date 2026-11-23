// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.model.CreateAccountRequestTestBuilder;

/**
 * A service behind the gateway that accepts the request but does not answer is cut off after the
 * read timeout, so it cannot hold the request thread.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "properties.downstream-http.read-timeout="
        + DownstreamTimeoutComponentTest.TIMEOUT)
@WalletAccountMock
@ActiveProfiles("test")
@AutoConfigureRestTestClient
class DownstreamTimeoutComponentTest {

  static final String TIMEOUT = "500ms";
  private static final int SERVICE_DELAY_MILLIS = 5_000;

  @Autowired
  private RestTestClient client;

  @Autowired
  private ApplicationConfig applicationConfig;

  @Autowired
  private MeterRegistry meterRegistry;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer accountServer;

  @BeforeEach
  void setUp() {
    accountServer.stubFor(post("/v0/accounts")
        .willReturn(aResponse().withStatus(201).withFixedDelay(SERVICE_DELAY_MILLIS)));
  }

  @Test
  void a_service_that_does_not_answer_within_the_read_timeout_fails_the_request() {
    long start = System.nanoTime();

    createAccount().expectStatus().isEqualTo(500);

    assertThat(Duration.ofNanos(System.nanoTime() - start))
        .isLessThan(Duration.ofMillis(SERVICE_DELAY_MILLIS));
  }

  @Test
  void a_call_to_a_service_is_timed_by_uri_template() {
    createAccount();

    assertThat(meterRegistry.get("http.client.requests").tag("uri", "/v0/accounts").timer()
        .count()).isPositive();
  }

  private RestTestClient.ResponseSpec createAccount() {
    return client.post()
        .uri("/v0/accounts")
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(CreateAccountRequestTestBuilder.withDefaults().build())
        .exchange();
  }
}
