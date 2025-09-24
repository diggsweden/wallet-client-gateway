// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import se.digg.wallet.gateway.application.config.ApiKeyAuthFilter;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.model.CreateWuaDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(port = 8099))
class ControllerIntegrationTest {

  public static final String TEST_ATTRIBUTE_VALUE = "test attribute value";
  public static final String TEST_ATTRIBUTE_ID = "12345";

  @Autowired
  private WebTestClient restClient;

  @Value("${wiremock.server.baseUrl}")
  private String wireMockUrl;

  @Autowired
  private ApplicationConfig applicationConfig;

  @LocalServerPort
  private int port;

  @Test
  void testCreateAttributeHappyPath() throws Exception {
    var createAttributeDto = new CreateWuaDto(TEST_ATTRIBUTE_VALUE);

    stubFor(post("/")
        .withRequestBody(equalToJson("""
            {
              "value": "%s"
            }
            """.formatted(TEST_ATTRIBUTE_VALUE)))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "id": "%s",
                  "value": "%s"
                }
                """.formatted(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE))));

    var response = restClient.post()
        .uri("/wua")
        .bodyValue(createAttributeDto)
        .header(ApiKeyAuthFilter.API_KEY_HEADER, applicationConfig.apisecret())
        .exchange();

    response.expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            {
              "id": "%s",
              "value": "%s"
            }
            """.formatted(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE));
  }

  @Test
  void testGetAttributeNotFound() throws Exception {
    stubFor(get("/%s".formatted(TEST_ATTRIBUTE_ID))
        .willReturn(aResponse()
            .withStatus(404)));

    var response = restClient.get()
        .uri("/%s".formatted(TEST_ATTRIBUTE_ID))
        .header(ApiKeyAuthFilter.API_KEY_HEADER, applicationConfig.apisecret())
        .exchange();

    response.expectStatus()
        .isEqualTo(500);
  }

}
