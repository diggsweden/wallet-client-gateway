// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.application.model.CreateWuaDto;
import se.digg.wallet.gateway.application.model.WuaDto;
import se.digg.wallet.gateway.infrastructure.walletprovider.client.WalletProviderClient;

@Service
public class WuaService {
  private final Logger logger = LoggerFactory.getLogger(WuaService.class);

  private final WalletProviderClient walletProviderClient;
  private final WuaMapper wuaMapper;

  public WuaService(WalletProviderClient walletProviderClient, WuaMapper wuaMapper) {
    this.walletProviderClient = walletProviderClient;
    this.wuaMapper = wuaMapper;
  }

  public WuaDto createWua(CreateWuaDto createWuaDto) {

    var mapped = wuaMapper.toWalletProviderCreateWuaDto(createWuaDto);
    var result = walletProviderClient.createWua(mapped);
    if (logger.isDebugEnabled()) {
      logger.debug("Mapped request {} to new wua dto {}",
          createWuaDto.walletId(), result.substring(0, 10));
    }
    return new WuaDto(result);
  }
}
