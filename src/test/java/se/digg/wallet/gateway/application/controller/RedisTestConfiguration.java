// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import com.redis.testcontainers.RedisContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.utility.DockerImageName;

public abstract class RedisTestConfiguration {

  public static RedisContainer redisContainer() {
    return new RedisContainer(DockerImageName.parse("redis:7.2.1-alpine"));
  }

  public static void configureRedisPort(DynamicPropertyRegistry registry,
      RedisContainer redisContainer) {
    registry.add("spring.redis.host", () -> "localhost");
    registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
  }
}
