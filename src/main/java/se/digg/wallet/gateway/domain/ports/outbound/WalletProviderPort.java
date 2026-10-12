// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.ports.outbound;

import jakarta.annotation.Nullable;
import java.util.List;
import se.digg.wallet.gateway.domain.model.attestation.KeyAttestation;
import se.digg.wallet.gateway.domain.model.attestation.WalletInstanceAttestation;
import se.digg.wallet.gateway.domain.model.attestation.Wua;
import se.digg.wallet.gateway.domain.model.common.Jwk;

public interface WalletProviderPort {

  Wua createWalletUnitAttestation(Jwk walletKey, @Nullable String nonce);

  KeyAttestation createKeyAttestation(List<Jwk> keys, @Nullable String nonce);

  WalletInstanceAttestation createWalletInstanceAttestation(Jwk key);
}
