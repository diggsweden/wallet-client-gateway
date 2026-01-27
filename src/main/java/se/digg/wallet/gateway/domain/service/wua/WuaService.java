// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.wua;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.application.model.wua.CreateWuaDto;
import se.digg.wallet.gateway.application.model.wua.CreateWuaDtoV2;
import se.digg.wallet.gateway.application.model.wua.WuaDto;
import se.digg.wallet.gateway.infrastructure.account.client.WalletAccountClient;
import se.digg.wallet.gateway.infrastructure.walletprovider.client.WalletProviderClient;

@Service
public class WuaService {
  private final Logger logger = LoggerFactory.getLogger(WuaService.class);

  private final WalletProviderClient walletProviderClient;
  private final WuaMapper wuaMapper;
  private final WalletAccountClient walletAccountClient;

  public WuaService(WalletAccountClient walletAccountClient,
      WalletProviderClient walletProviderClient, WuaMapper wuaMapper) {
    this.walletAccountClient = walletAccountClient;
    this.walletProviderClient = walletProviderClient;
    this.wuaMapper = wuaMapper;
  }

  public WuaDto createWua(String accountId) {
    var mapped = wuaMapper.toWalletProviderCreateWuaDto(
        walletAccountClient
            .getAccount(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found")));
    var result = walletProviderClient.createWua(mapped);
    if (logger.isDebugEnabled()) {
      logger.debug("Mapped request from accountId: {} to new wua dto {}",
          accountId, result.substring(0, 10));
    }
    return new WuaDto(result);
  }

  @Deprecated(since = "0.3.1", forRemoval = true)
  public WuaDto createWua(CreateWuaDto createWuaDto) {

    var mapped = wuaMapper.toWalletProviderCreateWuaDto(createWuaDto);
    var result = walletProviderClient.createWua(mapped);
    if (logger.isDebugEnabled()) {
      logger.debug("Mapped request {} to new wua dto {}",
          createWuaDto.walletId(), result.substring(0, 10));
    }
    return new WuaDto(result);
  }


  public WuaDto createWuaV2(CreateWuaDtoV2 createWuaDto) {

    var mapped = wuaMapper.toWalletProviderCreateWuaDtoV2(createWuaDto);
    var result = walletProviderClient.createWuaV2(mapped);
    if (logger.isDebugEnabled()) {
      logger.debug("Mapped request {} to new wua dto {}",
          createWuaDto.walletId(), result.substring(0, 10));
    }
    return new WuaDto(result);
  }
}
