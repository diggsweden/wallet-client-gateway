// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.downstream.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.infrastructure.downstream.model.WalletProviderCreateWuaDto;
import se.digg.wallet.gateway.infrastructure.downstream.model.WalletProviderWuaDto;

@Component
public class WalletProviderClient {

  private final RestClient restClient;
  private final String serviceUrl;

  public WalletProviderClient(RestClient restClient, ApplicationConfig applicationConfig) {
    this.restClient = restClient;
    this.serviceUrl = applicationConfig.walletprovider().baseurl();
  }

  public WalletProviderWuaDto createAttribute(WalletProviderCreateWuaDto createWuaDto) {
    return restClient
        .post()
        .uri(serviceUrl)
        .body(createWuaDto)
        // .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(WalletProviderWuaDto.class);
  }

  public WalletProviderWuaDto getWua(String id) {
    return restClient
        .get()
        .uri(serviceUrl + "/" + id)
        .retrieve()
        .body(WalletProviderWuaDto.class);
  }
}
