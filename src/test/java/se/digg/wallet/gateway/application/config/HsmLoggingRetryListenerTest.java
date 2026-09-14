// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryState;
import org.springframework.core.retry.Retryable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({
    MockitoExtension.class,
    OutputCaptureExtension.class
})
public class HsmLoggingRetryListenerTest {

  private final HsmLoggingRetryListener retryListener = new HsmLoggingRetryListener();

  @Mock
  private RetryPolicy retryPolicy;

  @Mock
  private Retryable<?> retryable;

  @Mock
  private RetryState retryState;

  @Test
  void a_retry_attempt_is_logged_before_it_executes(CapturedOutput console) {

    int retryCount = 0;
    when(retryState.getRetryCount()).thenReturn(retryCount);
    when(retryState.getLastException())
        .thenReturn(restClientResponseException(HttpStatus.SERVICE_UNAVAILABLE));

    retryListener.beforeRetry(retryPolicy, retryable, retryState);

    assertThat(console.getOut()).contains("retry attempt #%d".formatted(retryCount));
  }

  @Test
  void should_log_on_retry_success(CapturedOutput console) {

    retryListener.onRetrySuccess(retryPolicy, retryable, new Object());

    assertThat(console.getOut()).contains("retry attempt successful");
  }

  @Test
  void should_log_when_all_retry_attempts_have_failed(CapturedOutput console) {

    when(retryPolicy.shouldRetry(any())).thenReturn(true);

    retryListener.onRetryPolicyExhaustion(retryPolicy, retryable,
        new RetryException("", restClientResponseException(HttpStatus.SERVICE_UNAVAILABLE)));

    assertThat(console.getOut()).contains("fail");
  }

  @Test
  void should_log_when_retry_attempts_have_been_interrupted(CapturedOutput console) {

    retryListener.onRetryPolicyInterruption(retryPolicy, retryable,
        new RetryException("", new RuntimeException()));

    assertThat(console.getOut()).contains("interrupt");
  }

  @Test
  void should_log_when_configured_timeout_limit_has_been_exceeded(CapturedOutput console) {

    retryListener.onRetryPolicyTimeout(retryPolicy, retryable,
        new RetryException("", new RuntimeException()));

    assertThat(console.getOut()).contains("timeout");
  }

  private static Throwable restClientResponseException(HttpStatus httpStatus) {

    return new RestClientResponseException(
        "Mocked exception - %d %s".formatted(httpStatus.value(), httpStatus.getReasonPhrase()),
        httpStatus,
        httpStatus.getReasonPhrase(),
        HttpHeaders.EMPTY,
        "".getBytes(StandardCharsets.UTF_8),
        StandardCharsets.UTF_8);
  }
}
