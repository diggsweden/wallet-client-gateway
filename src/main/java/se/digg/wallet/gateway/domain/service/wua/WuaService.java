// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.wua;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.domain.model.wua.Wua;
import se.digg.wallet.gateway.domain.ports.outbound.AccountPort;
import se.digg.wallet.gateway.domain.ports.outbound.WalletProviderPort;

@Service
public class WuaService {
  private final Logger logger = LoggerFactory.getLogger(WuaService.class);

  private final AccountPort accountPort;
  private final WalletProviderPort walletProviderPort;

  public WuaService(AccountPort accountPort, WalletProviderPort walletProviderPort) {
    this.accountPort = accountPort;
    this.walletProviderPort = walletProviderPort;
  }

  public Wua createWua(String accountId, String nonce) {

    logger.info("Create WUA for accountId: {}, nonce: {}", accountId, nonce);
    var walletKey = accountPort.getWalletKey(accountId);

    return walletProviderPort.createWalletUnitAttestation(walletKey, nonce);
  }
}
