// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import se.digg.wallet.gateway.api.v0.model.ApiInfoResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiInfoControllerIntegrationTest {

  @LocalServerPort
  private int port;

  @Test
  void containsInfoValues() {
    RestTestClient restClient = RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build();

    var apiInfo = restClient.get()
        .uri("/api-info")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(ApiInfoResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(apiInfo).isNotNull();
    assertThat(apiInfo.getName()).isNotEmpty();
    assertThat(apiInfo.getVersion()).isNotEmpty();
    assertThat(apiInfo.getReleaseDate()).isAfter(LocalDate.EPOCH);
    assertThat(apiInfo.getStatus()).isNotEmpty();
  }
}
