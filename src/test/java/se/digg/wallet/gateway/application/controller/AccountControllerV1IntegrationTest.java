// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.EMAIL_ADDRESS;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.PERSONAL_IDENTITY_NUMBER;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.TELEPHONE_NUMBER;

import tools.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.redis.testcontainers.RedisContainer;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.AuthorizationServerMock;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder;
import se.digg.wallet.gateway.application.model.JwkDtoTestBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@AuthorizationServerMock
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureRestTestClient
class AccountControllerV1IntegrationTest {

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  @Autowired
  private RestTestClient restClient;

  @Autowired
  private ObjectMapper objectMapper;

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer walletAccountServer;

  @InjectWireMock(AuthorizationServerMock.NAME)
  private WireMockServer authorizationServer;

  private boolean authenticated;

  @BeforeEach
  public void beforeEach() throws Exception {
    if (!authenticated) {
      restClient = AuthUtil.oauth2Login(port, authorizationServer, restClient);
      authenticated = true;
    }
  }

  @Test
  void testCreateAccount() throws Exception {
    var generatedAccountId = UUID.randomUUID();
    var jwkString = objectMapper
        .writeValueAsString(JwkDtoTestBuilder.withDefaults().build());

    walletAccountServer.stubFor(post("/account")
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

  @Test
  void cannotCreateAccountsTwiceWithSameSession() throws Exception {
    testCreateAccount();
    assertThrows(AssertionError.class, this::testCreateAccount);
  }

  @Test
  void testAccountReturns500IfAccountServiceRespondsWith404() {
    walletAccountServer.stubFor(post("/account")
        .willReturn(aResponse()
            .withStatus(404)));

    var requestBody = CreateAccountRequestDtoTestBuilder.withDefaults().build();
    var response = restClient.post()
        .uri("/oidc/accounts/v1")
        .body(requestBody)
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
        .body(requestBody)
        .exchange();

    response.expectStatus()
        .isEqualTo(400);
  }

}
