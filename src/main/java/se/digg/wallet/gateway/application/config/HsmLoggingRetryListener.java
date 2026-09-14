// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryListener;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryState;
import org.springframework.core.retry.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
public class HsmLoggingRetryListener implements RetryListener {

  private static final Logger logger = LoggerFactory.getLogger(HsmLoggingRetryListener.class);
  private static final String LOG_PREFIX = "HSM remote service call";

  /*
   * Runs before each retry (not the initial one)
   */
  @Override
  public void beforeRetry(RetryPolicy retryPolicy, Retryable<?> retryable, RetryState retryState) {

    var exception = retryState.getLastException();
    var statusMessage = getResponseStatusMessage(exception);
    int retryCount = retryState.getRetryCount();
    logger.info("{} - failed with {} - retry attempt #{}", LOG_PREFIX, statusMessage, retryCount);
  }

  /*
   * Runs after a successful retry
   */
  @Override
  public void onRetrySuccess(RetryPolicy retryPolicy, Retryable<?> retryable,
      @Nullable Object result) {

    logger.info("{} - retry attempt successful", LOG_PREFIX);
  }

  /*
   * Runs after all attempts have been failed
   */
  @Override
  public void onRetryPolicyExhaustion(RetryPolicy retryPolicy, Retryable<?> retryable,
      RetryException exception) {

    var cause = exception.getCause();
    var statusMessage = getResponseStatusMessage(cause);
    if (retryPolicy.shouldRetry(cause)) {
      logger.error("{} - failed with {} - maximum retries exceeded, no more retries",
          LOG_PREFIX, statusMessage);
    } else {
      logger.error("{} - failed with {}", LOG_PREFIX, statusMessage);
    }
  }

  /*
   * Runs when retry are interrupted between two attempts
   */
  @Override
  public void onRetryPolicyInterruption(RetryPolicy retryPolicy, Retryable<?> retryable,
      RetryException exception) {

    logger.error("{} - retry attempt interruption", LOG_PREFIX, exception);
  }

  /*
   * Runs if the configured timeout limit has been exceeded
   */
  @Override
  public void onRetryPolicyTimeout(RetryPolicy retryPolicy, Retryable<?> retryable,
      RetryException exception) {

    logger.error("{} - retry timeout", LOG_PREFIX, exception);
  }

  private String getResponseStatusMessage(Throwable throwable) {

    if (throwable instanceof RestClientResponseException ex) {
      return "%d %s".formatted(ex.getStatusCode().value(), ex.getStatusText());
    } else {
      return throwable.getLocalizedMessage();
    }
  }
}
