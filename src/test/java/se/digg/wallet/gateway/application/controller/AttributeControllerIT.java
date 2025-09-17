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
import se.digg.wallet.gateway.application.model.CreateAttributeDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(port = 8099))
class AttributeControllerIT {

  public static final String TEST_ATTRIBUTE_VALUE = "test attribute value";
  public static final String TEST_ATTRIBUTE_ID = "12345";

  @Autowired
  private WebTestClient restClient;

  @Value("${wiremock.server.baseUrl}")
  private String wireMockUrl;

  @Value("${properties.apiSecret}")
  private String apiKey;

  @LocalServerPort
  private int port;

  @Test
  void testCreateAttribute_HappyPath() throws Exception {
    var createAttributeDto = new CreateAttributeDto(TEST_ATTRIBUTE_VALUE);

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
        .uri("/attributes")
        .bodyValue(createAttributeDto)
        .header(ApiKeyAuthFilter.API_KEY_HEADER, apiKey)
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
  void testGetAttribute_NotFound() throws Exception {
    stubFor(get("/%s".formatted(TEST_ATTRIBUTE_ID))
        .willReturn(aResponse()
            .withStatus(404)));

    var response = restClient.get()
        .uri("/%s".formatted(TEST_ATTRIBUTE_ID))
        .header(ApiKeyAuthFilter.API_KEY_HEADER, apiKey)
        .exchange();

    response.expectStatus()
        .isEqualTo(500);
  }

  // TODO do we need to test this config?
  // @Test
  // void testSwaggerEndpointsArePublic() throws Exception {
  // mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
  // mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
  // mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
  // }
}
