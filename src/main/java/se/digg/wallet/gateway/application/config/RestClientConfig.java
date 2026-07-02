
// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import se.digg.wallet.gateway.client.account.v0.api.AccountApi;
import se.digg.wallet.gateway.client.hsm.v1.api.HandlersApi;

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

  /**
   * Creates a client bean to be used for remote calls to the Wallet Account API.
   *
   * @return an AccountAPI instance.
   */
  @Bean
  public AccountApi accountApi(@Value("${properties.walletaccount.baseurl}") String basePath) {

    var accountApi = new AccountApi();
    accountApi.getApiClient()
        .setBasePath(basePath);

    return accountApi;
  }

  /**
   * Creates a client bean to be used for remote calls to the Wallet HSM API. (aka
   * wallet-r2ps/wallet-bff).
   *
   * @return an HsmAPI instance.
   */
  @Bean
  public HandlersApi hsmApi(@Value("${properties.wallet-r2ps.baseurl}") String basePath) {

    var api = new HandlersApi();
    api.getApiClient()
        .setBasePath(basePath);

    return api;
  }
}
