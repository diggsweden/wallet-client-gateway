// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.EMAIL_ADDRESS;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.PERSONAL_IDENTITY_NUMBER;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.PUBLIC_KEY_BASE64;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.PUBLIC_KEY_IDENTIFIER;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.TELEPHONE_NUMBER;

import java.util.UUID;
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
import se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(port = 8099))
class AccountControllerIntegrationTest {

  @Autowired
  private WebTestClient restClient;

  @Value("${wiremock.server.baseUrl}")
  private String wireMockUrl;

  @Autowired
  private ApplicationConfig applicationConfig;

  @LocalServerPort
  private int port;


  @Test
  void testRequestingWuaSuccessfullyReturnsCreated() {
    var generatedAccountId = UUID.randomUUID();

    stubFor(post("/account")
        .withRequestBody(equalToJson("""
            {
              "personalIdentityNumber": "%s",
              "emailAdress": "%s",
              "telephoneNumber": "%s",
              "publicKey": {
                "publicKeyBase64": "%s",
                "publicKeyIdentifier": "%s"
              }
            }
            """.formatted(PERSONAL_IDENTITY_NUMBER, EMAIL_ADDRESS, TELEPHONE_NUMBER,
            PUBLIC_KEY_BASE64, PUBLIC_KEY_IDENTIFIER)))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "id": "%s",
                  "personalIdentityNumber": "%s",
                  "emailAdress": "%s",
                  "telephoneNumber": "%s",
                  "publicKey": {
                    "publicKeyBase64": "%s",
                    "publicKeyIdentifier": "%s"
                  }
                }
                """.formatted(generatedAccountId, PERSONAL_IDENTITY_NUMBER, EMAIL_ADDRESS,
                TELEPHONE_NUMBER, PUBLIC_KEY_BASE64, PUBLIC_KEY_IDENTIFIER))));

    var requestBody = CreateAccountRequestDtoTestBuilder.withDefaults().build();
    var response = restClient.post()
        .uri("/accounts")
        .bodyValue(requestBody)
        .header(ApiKeyAuthFilter.API_KEY_HEADER, applicationConfig.apisecret())
        .exchange();

    response.expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            {
              "accountId": "%s"
            }
            """.formatted(generatedAccountId));
  }

  @Test
  void testRequestingWuaFailsReturnsInternalServerError() {
    stubFor(post("/account")
        .willReturn(aResponse()
            .withStatus(404)));

    var requestBody = CreateAccountRequestDtoTestBuilder.withDefaults().build();
    var response = restClient.post()
        .uri("/accounts")
        .bodyValue(requestBody)
        .header(ApiKeyAuthFilter.API_KEY_HEADER, applicationConfig.apisecret())
        .exchange();

    response.expectStatus()
        .isEqualTo(500);
  }

  @Test
  void testValidation() {
    var requestBody = CreateAccountRequestDtoTestBuilder.withDefaults()
        .emailAdress(null)
        .build();
    var response = restClient.post()
        .uri("/accounts")
        .bodyValue(requestBody)
        .header(ApiKeyAuthFilter.API_KEY_HEADER, applicationConfig.apisecret())
        .exchange();

    response.expectStatus()
        .isEqualTo(400);
  }

}
