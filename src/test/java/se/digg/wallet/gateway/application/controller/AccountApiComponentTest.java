// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import java.util.Objects;
import java.util.UUID;

import jakarta.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.WebApplicationContext;
import se.digg.wallet.gateway.api.v0.model.CreateAccountRequest;
import se.digg.wallet.gateway.api.v0.model.CreateAccountResponse;
import se.digg.wallet.gateway.api.v0.model.EcJwkRequest;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemParameterResponse;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.JwkBuilder;
import se.digg.wallet.gateway.domain.service.account.AccountService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountApiComponentTest {

  private static final String PERSONAL_IDENTITY_NUMBER = "2010101010";
  private static final String EMAIL = "test.testsson@test.test";
  private static final String PHONE_NUMBER = "0700000000";
  private static final String VALIDATION_FAILURE = "/problem-details/field-validation-failure";
  private static final UUID ACCOUNT_ID = UUID.fromString("61128b3c-ef55-4410-8dff-d8e8bf0cb9a7");
  private static final String KEY_ID = "26862913-ecd0-4d4d-a3d0-9271665d577e";
  private static final String TRANSACTION_ID = "a7240655-a568-41c8-8059-7b18859d5d88";

  @MockitoBean
  private AccountService accountService;

  private RestTestClient client;

  @BeforeEach
  void setUp(WebApplicationContext context) { // Inject the configuration
    client = RestTestClient.bindToApplicationContext(context).build();
    MDC.put("transactionId", TRANSACTION_ID);
  }

  @AfterEach
  void cleanUp() {
    MDC.clear();
  }

  @Test
  void createAccountWithoutDeviceKeyReturnsDeviceKeyProblem() {

    var problemResponse = client.post()
        .uri("/v0/accounts")
        .body(CreateAccountRequest.builder()
            .deviceKey(null)
            .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.BAD_REQUEST, VALIDATION_FAILURE, "deviceKey");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "deviceKey.kid",
      "deviceKey.kty",
      "deviceKey.crv",
      "deviceKey.x",
      "deviceKey.y"
  })
  void informsClientOfDeviceKeyParameterProblem(String invalidProperty) {

    var problemResponse = client.post()
        .uri("/v0/accounts")
        .body(CreateAccountRequest.builder()
            .deviceKey(EcJwkRequest.builder().build())
            .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.BAD_REQUEST, VALIDATION_FAILURE,
        invalidProperty);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      " ",
      "test@domain",
      "domain.xx",
      "test.testsson#domain.xx",
      "test.testsson@domain",
      "test.testsson.domain.se"
  })
  void createAccountWithBadEmailFormatReturnsEmailProblem(String badFormattedEmailAddress) {

    var problemResponse = client.post()
        .uri("/v0/accounts")
        .body(CreateAccountRequest.builder()
            .email(badFormattedEmailAddress)
            .deviceKey(defaultKeyRequest().build())
            .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.BAD_REQUEST, VALIDATION_FAILURE, "email");
  }

  @ParameterizedTest
  @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "INTERNAL_SERVER_ERROR"})
  void creatingAccountWhenRemoteServiceFailsWithBadRequestReturnsGenericServerProblem(
      HttpStatus httpStatus) {

    var restClientResponseException = new RestClientResponseException(
        "The remote error message",
        httpStatus,
        httpStatus.getReasonPhrase(),
        null, null, null);
    when(accountService.createAccount(any())).thenThrow(restClientResponseException);

    var problemResponse = client.post()
        .uri("/v0/accounts")
        .body(CreateAccountRequest.builder()
            .deviceKey(defaultKeyRequest().build())
            .build())
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void createAccountFailsWithUnexpectedServerErrorReturnsGenericServerProblem() {

    final var message = "The cause error message";
    var testException = new UnexpectedException(message);
    when(accountService.createAccount(any())).thenThrow(testException);

    var problemResponse = client.post()
        .uri("/v0/accounts")
        .body(CreateAccountRequest.builder()
            .deviceKey(defaultKeyRequest().build())
            .build())
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void createAccountWithoutOptionalsReturnsSavedValues() {

    final EcJwkRequest deviceKeyRequest = defaultKeyRequest().build();
    final Jwk deviceKeyDto = toPublicKeyDto(deviceKeyRequest);

    var accountDto = se.digg.wallet.gateway.domain.model.account.AccountBuilder.builder()
        .id(ACCOUNT_ID)
        .personalIdentityNumber(null)
        .emailAdress(null)
        .telephoneNumber(null)
        .deviceKey(deviceKeyDto)
        .build();
    when(accountService.createAccount(any())).thenReturn(accountDto);

    var accountResponse = client.post()
        .uri("/v0/accounts")
        .body(CreateAccountRequest.builder()
            .deviceKey(deviceKeyRequest)
            .build())
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(CreateAccountResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(accountResponse).isNotNull();
    assertThat(accountResponse.getAccountId()).isNotNull().isEqualTo(ACCOUNT_ID);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "a@a.se",
      "test@domain.com",
      "test.testsson@domain.xx",
      "test@domain.sub.eu",
      "a-very-long-firstname.and.another-long-lasting-lastname@sub-department.the-domain.net",
      "123@sub2.sub1.domain.com",
      "First.Last@Mixed.Se",
      "ONLY.CAPITAL.LETTERS@THE.DOMAIN.COM",
      "100@100.se"
  })
  void acceptsCreateAccountRequestsWithValidEmail(String email) {

    final EcJwkRequest deviceKeyRequest = defaultKeyRequest().build();
    final Jwk deviceKeyDto = toPublicKeyDto(deviceKeyRequest);

    var accountDto = se.digg.wallet.gateway.domain.model.account.AccountBuilder.builder()
        .id(ACCOUNT_ID)
        .emailAdress(email)
        .deviceKey(deviceKeyDto)
        .build();
    when(accountService.createAccount(any())).thenReturn(accountDto);

    var accountResponse = client.post()
        .uri("/v0/accounts")
        .body(CreateAccountRequest.builder()
            .personalIdentityNumber(null)
            .email(email)
            .telephoneNumber(null)
            .deviceKey(deviceKeyRequest)
            .build())
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(CreateAccountResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(accountResponse).isNotNull();
    assertThat(accountResponse.getAccountId()).isNotNull().isEqualTo(ACCOUNT_ID);
  }

  @Test
  void createAccountWithOptionalsReturnsAccountId() {

    final EcJwkRequest deviceKeyRequest = defaultKeyRequest().build();
    final Jwk deviceKeyDto = toPublicKeyDto(deviceKeyRequest);

    var accountDto = se.digg.wallet.gateway.domain.model.account.AccountBuilder.builder()
        .id(ACCOUNT_ID)
        .personalIdentityNumber(PERSONAL_IDENTITY_NUMBER)
        .emailAdress(EMAIL)
        .telephoneNumber(PHONE_NUMBER)
        .deviceKey(deviceKeyDto)
        .build();
    when(accountService.createAccount(any())).thenReturn(accountDto);

    var accountResponse = client.post()
        .uri("/v0/accounts")
        .body(CreateAccountRequest.builder()
            .personalIdentityNumber(PERSONAL_IDENTITY_NUMBER)
            .email(EMAIL)
            .telephoneNumber(PHONE_NUMBER)
            .deviceKey(deviceKeyRequest)
            .build())
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(CreateAccountResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(accountResponse).isNotNull();
    assertThat(accountResponse.getAccountId()).isNotNull().isEqualTo(ACCOUNT_ID);
  }

  private static EcJwkRequest.Builder defaultKeyRequest() {
    return EcJwkRequest.builder()
        .kid(KEY_ID)
        .kty("EC")
        .crv("P-256")
        .x("1fH0eqXgMMwCIafNaDc1axdCjLlw7zpTLvLWjpPvhEc")
        .y("5qOejJs7BK-jLingaUTEhBrzP_YPyHfptS5yWE98I40");
  }

  private static Jwk toPublicKeyDto(EcJwkRequest keyRequest) {
    return JwkBuilder.builder()
        .kty(keyRequest.getKty())
        .kid(keyRequest.getKid())
        .crv(keyRequest.getCrv())
        .x(keyRequest.getX())
        .y(keyRequest.getY())
        .build();
  }

  public static class UnexpectedException extends RuntimeException {

    public UnexpectedException(String message) {
      super(message);
    }
  }

  private static void assertProblemDetails(ProblemResponse problemResponse,
      HttpStatus expectedHttpStatus) {

    assertProblemDetails(problemResponse, expectedHttpStatus, null, null);
  }

  private static void assertProblemDetails(ProblemResponse problemResponse,
      HttpStatus expectedHttpStatus,
      @Nullable String expectedType,
      @Nullable String expectedInvalidParameterProperty) {

    assertThat(problemResponse).isNotNull();
    assertThat(problemResponse.getStatus()).isEqualTo(expectedHttpStatus.value());
    assertThat(problemResponse.getTitle()).isNotEmpty();
    assertThat(problemResponse.getDetail()).isPresent();
    assertThat(problemResponse.getInstance()).isNotEmpty();
    assertThat(problemResponse.getType()).isPresent();
    assertThat(problemResponse.getTransactionId()).isPresent().get().isEqualTo(TRANSACTION_ID);
    if (expectedType != null) {
      assertThat(problemResponse.getType()).get().isEqualTo(expectedType);
    }

    if (expectedInvalidParameterProperty != null) {
      assertThat(problemResponse.getInvalidParameters()).isNotEmpty();
      assertThat(expectedInvalidParameterProperty).isIn(problemResponse.getInvalidParameters()
          .stream()
          .map(ProblemParameterResponse::getProperty)
          .map(value -> value.orElse(null))
          .filter(Objects::nonNull)
          .toList());
    }
  }
}
