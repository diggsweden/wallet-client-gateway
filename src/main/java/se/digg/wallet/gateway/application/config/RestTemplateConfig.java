// SPDX-FileCopyrightText: 2025 diggsweden/wallet-backend-reference
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  /**
   * Creates a RestTemplate bean to be used for making HTTP requests. Since Spring Boot does not
   * provide one by default.
   *
   * @return a RestTemplate instance.
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
