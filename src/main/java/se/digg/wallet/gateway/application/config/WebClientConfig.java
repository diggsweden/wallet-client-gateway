// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  /**
   * Creates a WebClient.Builder bean to be used for making HTTP requests.
   *
   * @return a WebClient.Builder instance.
   */
  @Bean
  public WebClient webClient(WebClient.Builder builder) {
    return builder.build();
  }
}
