// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.application.model.CreateWuaDto;
import se.digg.wallet.gateway.application.model.WuaDto;
import se.digg.wallet.gateway.infrastructure.downstream.client.WalletProviderClient;
import se.digg.wallet.gateway.infrastructure.downstream.model.WalletProviderCreateWuaDto;

@Service
public class WuaService {

  private final WalletProviderClient walletProviderClient;

  public WuaService(WalletProviderClient walletProviderClient) {
    this.walletProviderClient = walletProviderClient;
  }

  public WuaDto createWua(CreateWuaDto createWuaDto) {
    var mapped = new WalletProviderCreateWuaDto(createWuaDto.value());
    var result = walletProviderClient.createAttribute(mapped);
    return new WuaDto(result.id(), result.value());
  }

  public WuaDto getWua(String id) {
    var result = walletProviderClient.getWua(id);
    return new WuaDto(result.id(), result.value());
  }
}
