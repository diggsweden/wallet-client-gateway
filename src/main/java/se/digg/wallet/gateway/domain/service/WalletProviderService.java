// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import jakarta.annotation.Nullable;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.domain.model.common.Jwk;
import se.digg.wallet.gateway.domain.model.walletprovider.KeyAttestation;
import se.digg.wallet.gateway.domain.model.walletprovider.Wua;
import se.digg.wallet.gateway.domain.ports.outbound.AccountPort;
import se.digg.wallet.gateway.domain.ports.outbound.WalletProviderPort;

@Service
public class WalletProviderService {
  private final Logger logger = LoggerFactory.getLogger(WalletProviderService.class);

  @Autowired
  private AccountPort accountPort;

  @Autowired
  private WalletProviderPort walletProviderPort;

  public Wua createWua(String accountId, String nonce) {

    logger.info("Create WUA for accountId: {}, nonce: {}", accountId, nonce);
    var walletKey = accountPort.getWalletKey(accountId);

    return walletProviderPort.createWalletUnitAttestation(walletKey, nonce);
  }

  public KeyAttestation createKeyAttestation(List<Jwk> keys, @Nullable String nonce) {

    return walletProviderPort.createKeyAttestation(keys, nonce);
  }
}
