// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  /**
   * Creates a RestClient bean to be used for making HTTP requests.
   *
   * @return a RestClient instance.
   */
  @Bean
  public RestClient webClient() {
    return RestClient.create();
  }
}
