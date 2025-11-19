// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.EMAIL_ADDRESS;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.PERSONAL_IDENTITY_NUMBER;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.TELEPHONE_NUMBER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder;
import se.digg.wallet.gateway.application.model.JwkDtoTestBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock({
    // Same wiremock for AS and services
    @ConfigureWireMock(
        port = 0,
        globalTemplating = true)
})
@Testcontainers
class AccountControllerV2IntegrationTest {

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  @Autowired
  private WebTestClient restClient;

  @Autowired
  private ObjectMapper objectMapper;

  @LocalServerPort
  private int port;

  @Value("${wiremock.server.port}")
  private int wiremockPort;

  private boolean authenticated;

  @BeforeEach
  public void beforeEach() throws Exception {
    if (!authenticated) {
      restClient = AuthUtil.oauth2Login(port, wiremockPort, restClient);
      authenticated = true;
    }
  }

  @Test
  void testCreateAccount() throws Exception {
    var generatedAccountId = UUID.randomUUID();
    var jwkString = objectMapper
        .writeValueAsString(JwkDtoTestBuilder.withDefaults().build());

    stubFor(post("/account")
        .withRequestBody(equalToJson("""
            {
              "personalIdentityNumber": "%s",
              "emailAdress": "%s",
              "telephoneNumber": "%s",
              "publicKey": %s
            }
            """.formatted(PERSONAL_IDENTITY_NUMBER,
            EMAIL_ADDRESS, TELEPHONE_NUMBER,
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
                """.formatted(generatedAccountId,
                PERSONAL_IDENTITY_NUMBER,
                EMAIL_ADDRESS,
                TELEPHONE_NUMBER, jwkString))));

    var requestBody = CreateAccountRequestDtoTestBuilder.withDefaults()
        .personalIdentityNumber(Optional.empty())
        .build();
    var response = restClient.post()
        .uri("/oidc/accounts/v1")
        .bodyValue(requestBody)
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
  void cannotCreateAccountsTwiceWithSameSession() throws Exception {
    testCreateAccount();
    assertThrows(AssertionError.class, this::testCreateAccount);
  }

  @Test
  void testAccountReturns500IfAccountServiceRespondsWith404() {
    stubFor(post("/account")
        .willReturn(aResponse()
            .withStatus(404)));

    var requestBody = CreateAccountRequestDtoTestBuilder.withDefaults().build();
    var response = restClient.post()
        .uri("/oidc/accounts/v1")
        .bodyValue(requestBody)
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
        .uri("/oidc/accounts/v1")
        .bodyValue(requestBody)
        .exchange();

    response.expectStatus()
        .isEqualTo(400);
  }

}
