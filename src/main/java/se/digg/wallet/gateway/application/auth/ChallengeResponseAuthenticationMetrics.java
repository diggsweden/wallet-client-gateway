// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Counts validated challenge responses by outcome: {@code success} when a session is created,
 * {@code failure} when the signed challenge is rejected. Both are registered at startup so the
 * series exist before the first login.
 */
@Component
public class ChallengeResponseAuthenticationMetrics {

  static final String NAME = "gateway.auth.challenge_response";

  private final Counter success;
  private final Counter failure;

  public ChallengeResponseAuthenticationMetrics(MeterRegistry registry) {
    this.success = counter(registry, "success");
    this.failure = counter(registry, "failure");
  }

  private static Counter counter(MeterRegistry registry, String outcome) {
    return Counter.builder(NAME)
        .description("Validated challenge-response authentication attempts")
        .tag("outcome", outcome)
        .register(registry);
  }

  public void recordSuccess() {
    success.increment();
  }

  public void recordFailure() {
    failure.increment();
  }
}
