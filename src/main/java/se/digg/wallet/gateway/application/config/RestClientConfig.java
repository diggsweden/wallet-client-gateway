
// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.digg.wallet.gateway.client.account.v0.api.AccountApi;
import se.digg.wallet.gateway.client.hsm.v1.api.HandlersApi;
import se.digg.wallet.gateway.client.provider.v0.api.WalletUnitAttestationApi;

@Configuration
public class RestClientConfig {

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

  /**
   * Creates a client bean to be used for remote calls to the Wallet Provider API.
   *
   * @return an WalletUnitAttestationAPI instance.
   */
  @Bean
  public WalletUnitAttestationApi walletUnitAttestationApiApi(
      @Value("${properties.walletprovider.baseurl}") String basePath) {

    var walletUnitAttestationApi = new WalletUnitAttestationApi();
    walletUnitAttestationApi.getApiClient()
        .setBasePath(basePath);

    return walletUnitAttestationApi;
  }
}
