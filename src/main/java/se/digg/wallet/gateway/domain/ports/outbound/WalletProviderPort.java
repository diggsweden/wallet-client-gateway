// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.ports.outbound;

import jakarta.annotation.Nullable;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.wua.Wua;

public interface WalletProviderPort {

  Wua createWalletUnitAttestation(Jwk walletKey, @Nullable String nonce);

}
