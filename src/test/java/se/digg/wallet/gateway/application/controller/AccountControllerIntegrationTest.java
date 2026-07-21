// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.EMAIL_ADDRESS;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.PERSONAL_IDENTITY_NUMBER;
import static se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder.TELEPHONE_NUMBER;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.api.v0.model.CreateAccountRequest;
import se.digg.wallet.gateway.api.v0.model.CreateAccountResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder;
import se.digg.wallet.gateway.application.model.CreateAccountRequestTestBuilder;
import se.digg.wallet.gateway.application.model.KeyRequestTestBuilder;
import se.digg.wallet.gateway.client.account.v0.model.AccountRequest;
import se.digg.wallet.gateway.client.account.v0.model.AccountResponse;
import se.digg.wallet.gateway.client.account.v0.model.KeyRequest;
import se.digg.wallet.gateway.client.account.v0.model.KeyResponse;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@ActiveProfiles("test")
@AutoConfigureRestTestClient
class AccountControllerIntegrationTest {

  private static final UUID ACCOUNT_ID = UUID.fromString("61128b3c-ef55-4410-8dff-d8e8bf0cb9a7");
  private static final String KEY_ID = "26862913-ecd0-4d4d-a3d0-9271665d577e";

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

  @BeforeEach
  void startup() {
    WireMock.resetAllScenarios();
  }

  @Test
  void testCreateAccount() throws Exception {
    var generatedAccountId = stubAccountCreation();

    var response = restClient.post()
        .uri("/v0/accounts")
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(CreateAccountRequestTestBuilder.withDefaults().build())
        .exchange();

    expectCreatedWithAccountId(response, generatedAccountId);
  }

  @Test
  void testCreateAccountWithEmptyEmailAndSsn() throws Exception {
    var generatedAccountId = stubAccountCreationWithNullValues();
    var accountWithEmptyEmailAndSsn = CreateAccountRequest.builder()
        .deviceKey(KeyRequestTestBuilder.withDefaults().build())
        .build();
    var response = restClient.post()
        .uri("/v0/accounts")
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(accountWithEmptyEmailAndSsn)
        .exchange();

    expectCreatedWithAccountId(response, generatedAccountId);
  }

  @Test
  void remoteServiceFailure() {
    server.stubFor(post("/account")
        .willReturn(aResponse()
            .withStatus(400)));

    var response = restClient.post()
        .uri("v0/accounts")
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(CreateAccountRequestDtoTestBuilder.withDefaults().build())
        .exchange();

    response.expectStatus()
        .isEqualTo(500);
  }

  @Test
  void testValidation() {
    var requestBody = CreateAccountRequestDtoTestBuilder.withDefaults()
        .deviceKey(null)
        .build();
    var response = restClient.post()
        .uri("v0/accounts")
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(requestBody)
        .exchange();

    response.expectStatus()
        .isEqualTo(400);
  }

  @Test
  void creatingDuplicateAccountReturnsConflictProblem() {

    var expectedRequest = AccountRequest.builder()
        .deviceKey(KeyRequest.builder()
            .kty("KTY")
            .kid(KEY_ID)
            .alg("ALG")
            .use("USE")
            .crv("CRV")
            .x("X")
            .y("Y")
            .build())
        .build();

    var expectedResponse = AccountResponse.builder()
        .id(ACCOUNT_ID)
        .deviceKey(KeyResponse.builder()
            .kty("KTY").kid(KEY_ID).alg("ALG").use("USE")
            .crv("CRV").x("X").y("Y").build())
        .build();

    var scenarioName = "Create Duplicate Account Sequence";

    // First request - create new account
    stubFor(post("/v0/accounts")
        .inScenario(scenarioName).whenScenarioStateIs(Scenario.STARTED)
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(expectedRequest), true, true))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "application/json")
            .withBody(objectMapper.writeValueAsString(expectedResponse)))
        .willSetStateTo("Account Created"));

    // Second request - fails with duplicate account
    stubFor(
        post("/v0/accounts")
            .inScenario(scenarioName)
            .whenScenarioStateIs("Account Created")
            .withRequestBody(
                equalToJson(objectMapper.writeValueAsString(expectedRequest), true, true))
            .willReturn(
                aResponse()
                    .withStatus(409)
                    .withHeader("content-type", "application/problem+json")
                    .withBody(
                        objectMapper.writeValueAsString(
                            ProblemResponse.builder()
                                .status(HttpStatus.CONFLICT.value())
                                .title(HttpStatus.CONFLICT.getReasonPhrase())
                                .build())))
            .willSetStateTo("Account Conflict"));

    var createdAccount = restClient.post()
        .uri("v0/accounts")
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(expectedRequest)
        .exchange()
        .expectStatus().isCreated()
        .expectBody(CreateAccountResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(createdAccount).isNotNull();
    assertThat(createdAccount.getAccountId()).isNotNull().isEqualTo(ACCOUNT_ID);

    var problemResponse = restClient.post()
        .uri("v0/accounts")
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(expectedRequest)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CONFLICT.value())
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(problemResponse).isNotNull();
    assertThat(problemResponse.getStatus()).isNotNull().isEqualTo(HttpStatus.CONFLICT.value());
  }

  private UUID stubAccountCreationWithNullValues() throws Exception {
    var generatedAccountId = UUID.randomUUID();

    var expectedRequest = Map.of("deviceKey", Map.of(
        "kty", "KTY",
        "kid", "KID",
        "alg", "ALG",
        "use", "USE",
        "crv", "CRV",
        "x", "X",
        "y", "Y"));

    var expectedResponse = AccountResponse.builder()
        .id(generatedAccountId)
        .email(null)
        .phoneNumber(null)
        .deviceKey(KeyResponse.builder()
            .kty("KTY").kid("KID").alg("ALG").use("USE")
            .crv("CRV").x("X").y("Y").build())
        .build();

    server.stubFor(post("/v0/accounts")
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(expectedRequest), true, true))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "application/json")
            .withBody(objectMapper.writeValueAsString(expectedResponse))));

    return generatedAccountId;
  }

  private UUID stubAccountCreation() throws Exception {
    var generatedAccountId = UUID.randomUUID();

    var expectedRequest = AccountRequest.builder()
        .personalIdentityNumber(PERSONAL_IDENTITY_NUMBER)
        .email(EMAIL_ADDRESS)
        .phoneNumber(TELEPHONE_NUMBER)
        .deviceKey(KeyRequest.builder()
            .kty("KTY").kid("KID").alg("ALG").use("USE")
            .crv("CRV").x("X").y("Y").build())
        .build();

    var expectedResponse = AccountResponse.builder()
        .id(generatedAccountId)
        .email(EMAIL_ADDRESS)
        .phoneNumber(TELEPHONE_NUMBER)
        .deviceKey(KeyResponse.builder()
            .kty("KTY").kid("KID").alg("ALG").use("USE")
            .crv("CRV").x("X").y("Y").build())
        .build();

    server.stubFor(post("/v0/accounts")
        .withRequestBody(equalToJson(objectMapper.writeValueAsString(expectedRequest)))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "application/json")
            .withBody(objectMapper.writeValueAsString(expectedResponse))));

    return generatedAccountId;
  }

  private void expectCreatedWithAccountId(RestTestClient.ResponseSpec response, UUID accountId) {
    response.expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            {
              "accountId": "%s"
            }
            """.formatted(accountId));
  }
}
