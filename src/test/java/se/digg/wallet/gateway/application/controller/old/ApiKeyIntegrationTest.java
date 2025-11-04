// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.old;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.model.CreateWuaDtoTestBuilder;
import se.digg.wallet.gateway.application.model.JwkDtoTestBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(port = 0))
class ApiKeyIntegrationTest {

  @Autowired
  private WebTestClient restClient;

  @Autowired
  ApplicationConfig applicationConfig;


  public static String TEST_JWK_STRING;

  public static final UUID TEST_WALLET_ID = UUID.randomUUID();
  private static final String SIGNED_JWT = """
      eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlF1aW5\
      jeSBMYXJzb24iLCJpYXQiOjE1MTYyMzkwMjJ9.WcPGXClpKD7Bc1C0CCDA1060E2GGlTfamrd8-W0ghBE
      """;

  @BeforeAll
  public static void beforeAll() throws Exception {
    TEST_JWK_STRING =
        new ObjectMapper().writeValueAsString(
            new ObjectMapper()
                .writeValueAsString(JwkDtoTestBuilder.withDefaults().build()));
    // remove wrapped outer quotes
    TEST_JWK_STRING = TEST_JWK_STRING.substring(1, TEST_JWK_STRING.length() - 1);
  }

  @Test
  void testValidApiKey() throws Exception {
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
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .exchange();
    response.expectStatus()
        .isEqualTo(201);
  }

  @Test
  void testInvalidApiKey() throws Exception {
    var requestBody = CreateWuaDtoTestBuilder.withWalletId(TEST_WALLET_ID);
    var response = restClient.post()
        .uri("/wua")
        .bodyValue(requestBody)
        .header(SecurityConfig.API_KEY_HEADER, "SUPER-INVALID-KEY")
        .exchange();
    response.expectStatus()
        .isEqualTo(401);
  }

  @Test
  void testNoApiKey() throws Exception {
    var requestBody = CreateWuaDtoTestBuilder.withWalletId(TEST_WALLET_ID);
    var response = restClient.post()
        .uri("/wua")
        .bodyValue(requestBody)
        .exchange();
    response.expectStatus()
        .isEqualTo(401);
  }

}
