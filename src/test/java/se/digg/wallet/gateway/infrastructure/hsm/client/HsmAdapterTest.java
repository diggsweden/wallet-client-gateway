// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.client;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import se.digg.wallet.gateway.domain.exception.RemoteResourceNotFoundException;
import se.digg.wallet.gateway.client.hsm.v1.api.HandlersApi;
import se.digg.wallet.gateway.infrastructure.hsm.HsmTestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class HsmAdapterTest {

  private static final UUID REQUEST_ID = UUID.fromString("616adb0e-0b09-4af9-a5a3-0181a69e373b");
  private static final byte[] PROBLEM_RESPONSE_BODY = "".getBytes(StandardCharsets.UTF_8);
  private static final int ONCE = 1;
  private static final int TWICE = 2;
  private static final int THREE = 3;

  @MockitoBean
  private HandlersApi hsmApi;

  @Autowired
  private HsmAdapter hsmAdapter;

  private int maxRetries;

  static Throwable restClientResponseException(HttpStatus httpStatus) {

    return new RestClientResponseException(
        "Mocked exception - %d %s".formatted(httpStatus.value(), httpStatus.getReasonPhrase()),
        httpStatus,
        httpStatus.getReasonPhrase(),
        HttpHeaders.EMPTY,
        PROBLEM_RESPONSE_BODY,
        StandardCharsets.UTF_8);
  }

  static List<Throwable> unrecoverableClientResponseExceptions() {

    var exceptions = new ArrayList<Throwable>();

    exceptions.add(restClientResponseException(HttpStatus.BAD_REQUEST));
    exceptions.add(restClientResponseException(HttpStatus.INTERNAL_SERVER_ERROR));
    exceptions.add(new ResourceAccessException("Mocked ResourceAccessException"));
    exceptions.add(new RestClientException("Mocked RestClientException"));
    exceptions.add(new RuntimeException("Mocked RuntimeException"));

    return exceptions;
  }

  static List<Throwable> recoverableClientResponseExceptions() {

    var exceptions = new ArrayList<Throwable>();

    exceptions.add(restClientResponseException(HttpStatus.TOO_MANY_REQUESTS));
    exceptions.add(restClientResponseException(HttpStatus.SERVICE_UNAVAILABLE));

    return exceptions;
  }

  @ParameterizedTest
  @MethodSource("unrecoverableClientResponseExceptions")
  void an_unrecoverable_device_state_registration_failure_must_not_retry(
      Throwable unrecoverableException) {

    when(hsmApi.createState(any())).thenThrow(unrecoverableException);

    assertThatThrownBy(() -> hsmAdapter.registerState(HsmTestBuilder
        .deviceStateRegistrationWithDefaults().build())).isEqualTo(unrecoverableException);
    verify(hsmApi, times(ONCE)).createState(any());
  }

  @ParameterizedTest
  @MethodSource("recoverableClientResponseExceptions")
  void a_recoverable_device_state_registration_failure_must_succeed_on_retry(
      Throwable recoverableException) {

    when(hsmApi.createState(any()))
        .thenThrow(recoverableException)
        .thenReturn(HsmTestBuilder.newStateResponseWithDefaults().build());

    var result = hsmAdapter.registerState(HsmTestBuilder.deviceStateRegistrationWithDefaults()
        .build());

    assertThat(result).isNotNull();
    verify(hsmApi, times(TWICE)).createState(any());
  }

  @ParameterizedTest
  @MethodSource("recoverableClientResponseExceptions")
  void a_recoverable_device_state_registration_failure_must_throw_exception_when_exceeding_max_retries(
      Throwable recoverableException) {

    when(hsmApi.createState(any()))
        .thenThrow(recoverableException)
        .thenThrow(recoverableException)
        .thenThrow(recoverableException);

    assertThatThrownBy(() -> hsmAdapter.registerState(HsmTestBuilder
        .deviceStateRegistrationWithDefaults().build())).isEqualTo(recoverableException);
    verify(hsmApi, times(THREE)).createState(any());
  }

  @Test
  void device_state_successfully_registered() {

    when(hsmApi.createState(any()))
        .thenReturn(HsmTestBuilder.newStateResponseWithDefaults().build());

    var result = hsmAdapter.registerState(HsmTestBuilder.deviceStateRegistrationWithDefaults()
        .build());

    assertThat(result).isNotNull();
    verify(hsmApi, times(ONCE)).createState(any());
  }

  @ParameterizedTest
  @MethodSource("unrecoverableClientResponseExceptions")
  void an_unrecoverable_hsm_response_failure_must_not_retry(Throwable unrecoverableException) {

    when(hsmApi.service(any())).thenThrow(unrecoverableException);

    assertThatThrownBy(() -> hsmAdapter.submitAsync(HsmTestBuilder.hsmOperationWithDefaults()
        .build())).isEqualTo(unrecoverableException);
    verify(hsmApi, times(ONCE)).service(any());
  }

  @ParameterizedTest
  @MethodSource("recoverableClientResponseExceptions")
  void a_recoverable_hsm_response_failure_should_succeed_on_retry(Throwable recoverableException) {

    when(hsmApi.service(any()))
        .thenThrow(recoverableException)
        .thenReturn(HsmTestBuilder.asyncResponse().build());

    var result = hsmAdapter.submitAsync(HsmTestBuilder.hsmOperationWithDefaults().build());

    assertThat(result).isNotNull();
    verify(hsmApi, times(TWICE)).service(any());
  }

  @ParameterizedTest
  @MethodSource("recoverableClientResponseExceptions")
  void a_recoverable_hsm_response_failure_must_throw_exception_when_exceeding_max_retries(
      Throwable recoverableException) {

    when(hsmApi.service(any()))
        .thenThrow(recoverableException)
        .thenThrow(recoverableException)
        .thenThrow(recoverableException);

    assertThatThrownBy(() -> hsmAdapter.submitAsync(HsmTestBuilder.hsmOperationWithDefaults()
        .build())).isEqualTo(recoverableException);
    verify(hsmApi, times(THREE)).service(any());
  }

  @Test
  void hsm_request_successfully_created() {

    when(hsmApi.service(any())).thenReturn(HsmTestBuilder.asyncResponse().build());

    var result = hsmAdapter.submitAsync(HsmTestBuilder.hsmOperationWithDefaults().build());

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(REQUEST_ID);
    verify(hsmApi, times(ONCE)).service(any());
  }

  @ParameterizedTest
  @MethodSource("unrecoverableClientResponseExceptions")
  void an_unrecoverable_async_response_failure_must_not_retry(Throwable unrecoverableException) {

    when(hsmApi.taskResponse(any())).thenThrow(unrecoverableException);

    assertThatThrownBy(() -> hsmAdapter.getAsyncResult(REQUEST_ID))
        .isEqualTo(unrecoverableException);
    verify(hsmApi, times(ONCE)).taskResponse(any());
  }

  @ParameterizedTest
  @MethodSource("recoverableClientResponseExceptions")
  void a_recoverable_async_response_failure_must_succeed_on_retry(Throwable recoverableException) {

    when(hsmApi.taskResponse(any()))
        .thenThrow(recoverableException)
        .thenReturn(HsmTestBuilder.asyncResponse().build());

    var result = hsmAdapter.getAsyncResult(REQUEST_ID);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(REQUEST_ID);
    verify(hsmApi, times(TWICE)).taskResponse(any());
  }

  @ParameterizedTest
  @MethodSource("recoverableClientResponseExceptions")
  void a_recoverable_async_response_failure_must_throw_exception_when_exceeding_max_retries(
      Throwable recoverableException) {

    when(hsmApi.taskResponse(any()))
        .thenThrow(recoverableException)
        .thenThrow(recoverableException)
        .thenThrow(recoverableException);

    assertThatThrownBy(() -> hsmAdapter.getAsyncResult(REQUEST_ID))
        .isEqualTo(recoverableException);
    verify(hsmApi, times(THREE)).taskResponse(any());
  }

  @Test
  void an_async_response_not_found_must_throw_remote_resource_not_found_exception() {

    when(hsmApi.taskResponse(any()))
        .thenThrow(restClientResponseException(HttpStatus.NOT_FOUND));

    assertThatThrownBy(() -> hsmAdapter.getAsyncResult(REQUEST_ID))
        .isInstanceOf(RemoteResourceNotFoundException.class);
    verify(hsmApi, times(ONCE)).taskResponse(any());
  }

  @Test
  void serves_async_response() {

    when(hsmApi.taskResponse(any()))
        .thenReturn(HsmTestBuilder.asyncResponse().build());

    var result = assertDoesNotThrow(() -> hsmAdapter.getAsyncResult(REQUEST_ID));

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(REQUEST_ID);
    verify(hsmApi, times(ONCE)).taskResponse(any());
  }
}
