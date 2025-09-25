// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.walletprovider.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDto;

@Component
public class WalletProviderClient {

  private final RestClient restClient;
  private final String walletProviderUrl;
  private static String path = "/wallet-unit-attestation";

  public WalletProviderClient(RestClient restClient, ApplicationConfig applicationConfig) {
    this.restClient = restClient;
    this.walletProviderUrl = applicationConfig.walletprovider().baseurl();
  }

  public String createWua(WalletProviderCreateWuaDto createWuaDto) {
    return restClient
        .post()
        .uri(walletProviderUrl + path)
        .body(createWuaDto)
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(String.class);
  }

}
