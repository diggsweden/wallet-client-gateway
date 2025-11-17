// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;

@Configuration
public class SessionConfig {
  public static final String SESSION_HEADER = "SESSION";

  @Bean
  public LettuceConnectionFactory connectionFactory(
      @Value("${spring.redis.port}") int port,
      @Value("${spring.redis.host}") String host) {
    return new LettuceConnectionFactory(host, port);
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(
      RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
    var template = new RedisTemplate<String, String>();
    template.setConnectionFactory(redisConnectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new StringRedisSerializer());

    return template;
  }

  @Bean
  public HeaderHttpSessionIdResolver sessionIdResolver() {
    return new HeaderHttpSessionIdResolver(SESSION_HEADER);
  }
}
