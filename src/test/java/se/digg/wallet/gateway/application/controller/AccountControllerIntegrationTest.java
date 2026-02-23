// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.EMAIL_ADDRESS;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.PERSONAL_IDENTITY_NUMBER;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.TELEPHONE_NUMBER;

import com.github.tomakehurst.wiremock.WireMockServer;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder;
import se.digg.wallet.gateway.application.model.JwkDtoTestBuilder;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@ActiveProfiles("test")
@AutoConfigureRestTestClient
class AccountControllerIntegrationTest {

  public static final List<String> ACCOUNTS_PATH = List.of("accounts", "accounts/v1");

  @Autowired
  private RestTestClient restClient;

  @Value("${wiremock.server.baseUrl}")
  private String wireMockUrl;

  @Autowired
  private ObjectMapper objectMapper;

  @LocalServerPort
  private int port;

  @Autowired
  private ApplicationConfig applicationConfig;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer server;

  @ParameterizedTest
  @FieldSource("ACCOUNTS_PATH")
  void testCreateAccount(String path) throws Exception {
    var generatedAccountId = UUID.randomUUID();
    var jwkString = objectMapper.writeValueAsString(JwkDtoTestBuilder.withDefaults().build());

    server.stubFor(post("/account")
        .withRequestBody(equalToJson("""
            {
              "personalIdentityNumber": "%s",
              "emailAdress": "%s",
              "telephoneNumber": "%s",
              "publicKey": %s
            }
            """.formatted(PERSONAL_IDENTITY_NUMBER, EMAIL_ADDRESS, TELEPHONE_NUMBER,
            jwkString)))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "id": "%s",
                  "personalIdentityNumber": "%s",
                  "emailAdress": "%s",
                  "telephoneNumber": "%s",
                  "publicKey": %s
                }
                """.formatted(generatedAccountId, PERSONAL_IDENTITY_NUMBER, EMAIL_ADDRESS,
                TELEPHONE_NUMBER, jwkString))));

    var requestBody = CreateAccountRequestDtoTestBuilder.withDefaults().build();
    var response = restClient.post()
        .uri(path)
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(requestBody)
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

  @ParameterizedTest
  @FieldSource("ACCOUNTS_PATH")
  void testAccountReturns500IfAccountServiceRespondsWith404(String path) {
    server.stubFor(post("/account")
        .willReturn(aResponse()
            .withStatus(404)));

    var requestBody = CreateAccountRequestDtoTestBuilder.withDefaults().build();
    var response = restClient.post()
        .uri(path)
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(requestBody)
        .exchange();

    response.expectStatus()
        .isEqualTo(500);
  }

  @ParameterizedTest
  @FieldSource("ACCOUNTS_PATH")
  void testValidation(String path) {
    var requestBody = CreateAccountRequestDtoTestBuilder.withDefaults()
        .emailAdress(null)
        .build();
    var response = restClient.post()
        .uri(path)
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(requestBody)
        .exchange();

    response.expectStatus()
        .isEqualTo(400);
  }
}
