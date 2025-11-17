// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.auth.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.infrastructure.auth.model.AuthChallengeCacheValue;

@Component
public class ChallengeCache {

  private static final String REDIS_PREFIX = "wallet-client-gateway:challenge:";

  private final int ttlSeconds;

  private RedisTemplate<String, String> redisTemplate;
  private ObjectMapper objectMapper;

  public ChallengeCache(ApplicationConfig config,
      RedisTemplate<String, String> redisTemplate,
      ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.ttlSeconds = config.challengeCache().ttlSeconds();
  }

  public void store(AuthChallengeCacheValue challenge) {
    redisTemplate.opsForValue()
        .set(REDIS_PREFIX + challenge.nonce(), toJson(challenge), Duration.ofSeconds(ttlSeconds));
  }

  public Optional<AuthChallengeCacheValue> get(String nonce) {
    return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(REDIS_PREFIX + nonce))
        .map(this::fromJson);
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private AuthChallengeCacheValue fromJson(String json) {
    try {
      return objectMapper.readValue(json, AuthChallengeCacheValue.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
