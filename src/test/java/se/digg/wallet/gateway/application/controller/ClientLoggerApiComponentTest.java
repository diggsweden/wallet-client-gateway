// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static se.digg.wallet.gateway.application.controller.util.ProblemDetailAssertion.assertProblemDetails;

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;
import se.digg.wallet.gateway.api.v0.ClientUtilsApi;
import se.digg.wallet.gateway.api.v0.model.ClientLogRequest;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;
import se.digg.wallet.gateway.application.mapper.clientconfig.ClientContextMapper;
import se.digg.wallet.gateway.domain.model.clientlogger.ClientLogEntry;
import se.digg.wallet.gateway.domain.service.ClientConfigService;
import se.digg.wallet.gateway.domain.service.ClientLoggerService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ClientLoggerApiComponentTest {

  private static final String TRANSACTION_ID = "a7240655-a568-41c8-8059-7b18859d5d88";
  private static final String MESSAGE = "Client log message";

  @MockitoBean
  private ClientConfigService clientConfigService;

  @MockitoBean
  private ClientLoggerService clientLoggerService;

  private RestTestClient client;

  @BeforeEach
  void setUp(WebApplicationContext context) {
    client = RestTestClient.bindToApplicationContext(context).build();
    MDC.put("transactionId", TRANSACTION_ID);
  }

  @AfterEach
  void cleanUp() {
    MDC.clear();
  }

  @ParameterizedTest
  @EnumSource(ClientLogRequest.LevelEnum.class)
  void creates_a_client_log_at_each_level_with_client_metadata(ClientLogRequest.LevelEnum level) {
    client.post()
        .uri(ClientUtilsApi.PATH_CREATE_CLIENT_LOG)
        .header(ClientContextMapper.DEVICE_OS, "Android")
        .header(ClientContextMapper.DEVICE_OS_VERSION, "15")
        .header(ClientContextMapper.DEVICE_MODEL, "Pixel 9")
        .header(ClientContextMapper.APP_VERSION, "1.2.3")
        .header(ClientContextMapper.APP_BUILD, "42")
        .body(ClientLogRequest.builder().level(level).message(MESSAGE).build())
        .exchange()
        .expectStatus().isNoContent()
        .expectBody().isEmpty();

    verify(clientLoggerService).createClientLog(new ClientLogEntry(
        ClientLogEntry.LogLevel.valueOf(level.name()), MESSAGE,
        "Android", "15", "Pixel 9", "1.2.3", "42"));
  }

  @Test
  void defaults_to_info_when_log_level_and_client_headers_are_omitted() {
    client.post()
        .uri(ClientUtilsApi.PATH_CREATE_CLIENT_LOG)
        .body(Map.of("message", MESSAGE))
        .exchange()
        .expectStatus().isNoContent()
        .expectBody().isEmpty();

    verify(clientLoggerService).createClientLog(new ClientLogEntry(
        ClientLogEntry.LogLevel.INFO, MESSAGE, null, null, null, null, null));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void rejects_a_missing_or_empty_log_message(String message) {
    var problem = client.post()
        .uri(ClientUtilsApi.PATH_CREATE_CLIENT_LOG)
        .body(ClientLogRequest.builder().message(message).build())
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody(ProblemResponse.class)
        .returnResult().getResponseBody();

    assertProblemDetails(problem, HttpStatus.BAD_REQUEST, TRANSACTION_ID,
        "/problem-details/field-validation-failure", "message");
    verifyNoInteractions(clientLoggerService);
  }

  @Test
  void rejects_a_log_message_longer_than_the_maximum_length() {
    client.post()
        .uri(ClientUtilsApi.PATH_CREATE_CLIENT_LOG)
        .body(ClientLogRequest.builder().message("a".repeat(10001)).build())
        .exchange()
        .expectStatus().isBadRequest();

    verifyNoInteractions(clientLoggerService);
  }

  @Test
  void accepts_a_log_message_at_the_maximum_length() {
    var message = "a".repeat(10000);

    client.post()
        .uri(ClientUtilsApi.PATH_CREATE_CLIENT_LOG)
        .body(Map.of("message", message))
        .exchange()
        .expectStatus().isNoContent()
        .expectBody().isEmpty();

    verify(clientLoggerService).createClientLog(new ClientLogEntry(
        ClientLogEntry.LogLevel.INFO, message, null, null, null, null, null));
  }

  @Test
  void rejects_an_unknown_log_level() {
    client.post()
        .uri(ClientUtilsApi.PATH_CREATE_CLIENT_LOG)
        .body(Map.of("level", "UNKNOWN", "message", MESSAGE))
        .exchange()
        .expectStatus().isBadRequest();

    verifyNoInteractions(clientLoggerService);
  }

  @Test
  void rejects_a_missing_request_body() {
    client.post()
        .uri(ClientUtilsApi.PATH_CREATE_CLIENT_LOG)
        .contentType(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest();

    verifyNoInteractions(clientLoggerService);
  }

  @ParameterizedTest
  @MethodSource("clientLogFailures")
  void returns_no_content_even_when_the_logging_service_fails(Throwable failure) {
    doThrow(failure).when(clientLoggerService).createClientLog(any());

    client.post()
        .uri(ClientUtilsApi.PATH_CREATE_CLIENT_LOG)
        .body(Map.of("message", MESSAGE))
        .exchange()
        .expectStatus().isNoContent()
        .expectBody().isEmpty();

    verify(clientLoggerService).createClientLog(new ClientLogEntry(
        ClientLogEntry.LogLevel.INFO, MESSAGE, null, null, null, null, null));
  }

  private static Stream<Throwable> clientLogFailures() {
    return Stream.of(new IllegalStateException("Logging unavailable"),
        new AssertionError("Logging failed"));
  }
}
