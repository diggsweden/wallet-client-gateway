// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.auth.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.infrastructure.auth.model.AuthChallengeCacheValue;

@Component
public class ChallengeCache {

  private final int ttlSeconds;


  private Map<String, AuthChallengeCacheValue> cache = new HashMap<>();
  private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

  public ChallengeCache(ApplicationConfig config) {
    this.ttlSeconds = config.challengeCache().ttlSeconds();
  }

  public void store(AuthChallengeCacheValue challenge) {
    cache.put(challenge.nonce(), challenge);

    executorService.schedule(() -> cache.remove(challenge.nonce()), ttlSeconds,
        TimeUnit.SECONDS);
  }

  public Optional<AuthChallengeCacheValue> get(String nonce) {
    return Optional.ofNullable(cache.remove(nonce));
  }
}
