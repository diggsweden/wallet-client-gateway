// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClientResponseException;

@Configuration
public class HsmRetryConfig {

  @Value("${properties.resilience.retry-policy.max-retries:2}")
  private long maxRetries;

  @Value("${properties.resilience.retry-policy.delay-millis:1000}")
  private long delay;

  @Value("${properties.resilience.retry-policy.multiplier:1.5}")
  private double multiplier;

  private static final List<HttpStatusCode> RECOVERABLE_RESPONSE_STATUS_CODES =
      List.of(
          HttpStatus.TOO_MANY_REQUESTS,
          HttpStatus.SERVICE_UNAVAILABLE);

  @Bean(name = "hsmRetryTemplate")
  public RetryTemplate hsmRetryTemplate(HsmLoggingRetryListener retryListener) {

    var retryTemplate = new RetryTemplate(RetryPolicy.builder()
        .maxRetries(maxRetries)
        .delay(Duration.ofMillis(delay))
        .multiplier(multiplier)
        .includes(RestClientResponseException.class)
        .predicate(throwable -> {
          if (throwable instanceof RestClientResponseException ex) {
            var responseStatusCode = ex.getStatusCode();
            return RECOVERABLE_RESPONSE_STATUS_CODES.contains(responseStatusCode);
          }
          return false;
        })
        .build());

    retryTemplate.setRetryListener(retryListener);
    return retryTemplate;
  }
}
