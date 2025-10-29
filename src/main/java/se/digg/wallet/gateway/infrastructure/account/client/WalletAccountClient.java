// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.account.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountAccountDto;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountCreateAccountRequestDto;

@Component
public class WalletAccountClient {

  private final RestClient restClient;
  private final String baseUrl;
  private final String createAccountPath;

  public WalletAccountClient(RestClient restClient, ApplicationConfig applicationConfig) {
    this.restClient = restClient;
    this.baseUrl = applicationConfig.walletaccount().baseurl();
    this.createAccountPath = applicationConfig.walletaccount().paths().post();
  }

  public WalletAccountAccountDto createAccount(WalletAccountCreateAccountRequestDto dto) {
    return restClient
        .post()
        .uri(baseUrl + createAccountPath)
        .body(dto)
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(WalletAccountAccountDto.class);
  }

}
