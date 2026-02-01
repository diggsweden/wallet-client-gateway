// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.walletprovider.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDto;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDtoV1;

@Component
public class WalletProviderClient {

  private final RestClient restClient;
  private final String walletProviderUrl;
  private final String wuaPath;
  private final String wuaUrlV1;
  private final String wuaUrlV2;

  public WalletProviderClient(RestClient restClient, ApplicationConfig applicationConfig) {
    this.restClient = restClient.mutate().build();
    this.walletProviderUrl = applicationConfig.walletprovider().baseurl();
    this.wuaPath = applicationConfig.walletprovider().wuaPath();
    this.wuaUrlV1 = walletProviderUrl + wuaPath;
    this.wuaUrlV2 = walletProviderUrl + wuaPath + "/v2";
  }

  public String createWua(WalletProviderCreateWuaDto createWuaDto) {
    return restClient
        .post()
        .uri(wuaUrlV2)
        .body(createWuaDto)
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(String.class);
  }

  @Deprecated(since = "0.3.1", forRemoval = true)
  public String createWua(WalletProviderCreateWuaDtoV1 createWuaDto) {
    return restClient
        .post()
        .uri(wuaUrlV1)
        .body(createWuaDto)
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(String.class);
  }
}
