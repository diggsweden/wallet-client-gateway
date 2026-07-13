// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;
import se.digg.wallet.gateway.api.v0.model.ApiInfoResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiInfoComponentTest {

  private static RestTestClient client;

  @BeforeAll
  static void setUp(WebApplicationContext context) { // Inject the configuration
    client = RestTestClient.bindToApplicationContext(context).build();
  }

  @Test
  void servesApiInfo() {

    var apiInfoResponse = client.get()
        .uri("/api-info")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(ApiInfoResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(apiInfoResponse).isNotNull();
    assertThat(apiInfoResponse.getName()).isNotEmpty();
    assertThat(apiInfoResponse.getVersion()).isNotEmpty();
    assertThat(apiInfoResponse.getReleaseDate()).isNotNull();
    assertThat(apiInfoResponse.getReleaseDate()).isAfter(LocalDate.EPOCH);
    assertThat(apiInfoResponse.getStatus()).isNotEmpty();
  }
}
