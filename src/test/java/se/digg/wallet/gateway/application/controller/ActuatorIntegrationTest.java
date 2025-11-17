// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ActuatorIntegrationTest {


  @Container
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  @DynamicPropertySource
  static void configureRedisPort(DynamicPropertyRegistry registry) {
    RedisTestConfiguration.configureRedisPort(registry, redisContainer);
  }

  @Autowired
  private WebTestClient restClient;

  @Test
  void testActuatorHealthEndpoint() {
    var response = restClient.get()
        .uri("/actuator/health")
        .exchange();
    response.expectStatus().isOk();
  }

}
