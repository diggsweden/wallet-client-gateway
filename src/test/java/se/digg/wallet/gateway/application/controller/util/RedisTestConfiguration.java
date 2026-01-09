// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.util;

import com.redis.testcontainers.RedisContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class RedisTestConfiguration {

  public static RedisContainer redisContainer() {
    return new RedisContainer(DockerImageName.parse("valkey/valkey:9.0.0-alpine"));
  }

}
