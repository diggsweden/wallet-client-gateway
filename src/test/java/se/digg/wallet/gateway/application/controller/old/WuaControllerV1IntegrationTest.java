// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.old;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.util.WalletProviderMock;
import se.digg.wallet.gateway.application.model.CreateWuaDtoTestBuilder;
import se.digg.wallet.gateway.application.model.JwkDtoTestBuilder;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletProviderMock
@ActiveProfiles("test")
@AutoConfigureRestTestClient
class WuaControllerV1IntegrationTest {
  public static String TEST_JWK_STRING;

  public static final UUID TEST_WALLET_ID = UUID.randomUUID();
  private static final String SIGNED_JWT = """
      eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlF1aW5\
      jeSBMYXJzb24iLCJpYXQiOjE1MTYyMzkwMjJ9.WcPGXClpKD7Bc1C0CCDA1060E2GGlTfamrd8-W0ghBE
      """;
  @Autowired
  private RestTestClient restClient;

  @Autowired
  private ApplicationConfig applicationConfig;

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletProviderMock.NAME)
  private WireMockServer providerServer;

  @BeforeAll
  public static void beforeAll() throws Exception {
    TEST_JWK_STRING =
        new ObjectMapper().writeValueAsString(
            new ObjectMapper().writeValueAsString(JwkDtoTestBuilder.withDefaults().build()));
    // remove wrapped outer quotes
    TEST_JWK_STRING = TEST_JWK_STRING.substring(1, TEST_JWK_STRING.length() - 1);
  }

  @Test
  void testRequestingWuaSuccessfullyReturnsCreated() {
    providerServer.stubFor(post("/wallet-provider/wallet-unit-attestation")
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
        .body(requestBody)
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
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
    providerServer.stubFor(post("/wallet-provider/wallet-unit-attestation")
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
        .body(requestBody)
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .exchange();

    response.expectStatus()
        .isEqualTo(500);
  }

  @Test
  void testValidation() {
    var requestBody = CreateWuaDtoTestBuilder.invaliDto();
    var response = restClient.post()
        .uri("/wua")
        .body(requestBody)
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .exchange();

    response.expectStatus()
        .isEqualTo(400);
  }

}
