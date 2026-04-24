// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.ports.outbound;

import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelope;

public interface AccountPort {

  Account createAccount(NewAccount account);

  Account createAccountLegacy(NewAccount account);

  void addWalletKey(Jwk walletKey, String accountId);

  void addSecurityEnvelope(SecurityEnvelope securityEnvelope, String accountId);
}
