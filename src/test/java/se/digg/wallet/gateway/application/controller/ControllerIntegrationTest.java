// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

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
import se.digg.wallet.gateway.application.model.CreateWuaDtoTestBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(port = 8099))
class ControllerIntegrationTest {
  public static final String TEST_JWK_STRING =
      """
          {\\"kty\\":\\"kty\\",\\"kid\\":\\"kid\\",\\"alg\\":\\"alg\\",\\"use\\":\\"use\\",\
          \\"crv\\":\\"crv\\",\\"x\\":\\"x\\",\\"y\\":\\"y\\"}""";

  public static final UUID TEST_WALLET_ID = UUID.randomUUID();
  private static final String SIGNED_JWT = """
      eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlF1aW5\
      jeSBMYXJzb24iLCJpYXQiOjE1MTYyMzkwMjJ9.WcPGXClpKD7Bc1C0CCDA1060E2GGlTfamrd8-W0ghBE
      """;
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
    stubFor(post("/wallet-provider/wallet-unit-attestation")
        .withRequestBody(equalToJson("""
            {
              "walletId": "%s",
              "jwk": "%s"
            }
            """.formatted(TEST_WALLET_ID, TEST_JWK_STRING)))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "text/plain")
            .withBody(SIGNED_JWT)));

    var requestBody = CreateWuaDtoTestBuilder.withWalletId(TEST_WALLET_ID);
    var response = restClient.post()
        .uri("/wua")
        .bodyValue(requestBody)
        .header(ApiKeyAuthFilter.API_KEY_HEADER, applicationConfig.apisecret())
        .exchange();

    response.expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            {

              "jwt": "%s"
            }
            """.formatted(SIGNED_JWT));
  }

  @Test
  void testRequestingWuaFailsReturnsInternalServerError() {
    stubFor(post("/wallet-provider/wallet-unit-attestation")
        .withRequestBody(equalToJson("""
            {
              "walletId": "%s",
              "jwk": "%s"
            }
            """.formatted(TEST_WALLET_ID, TEST_JWK_STRING)))
        .willReturn(aResponse()
            .withStatus(404)));

    var requestBody = CreateWuaDtoTestBuilder.withWalletId(TEST_WALLET_ID);
    var response = restClient.post()
        .uri("/wua")
        .bodyValue(requestBody)
        .header(ApiKeyAuthFilter.API_KEY_HEADER, applicationConfig.apisecret())
        .exchange();

    response.expectStatus()
        .isEqualTo(500);
  }

}
