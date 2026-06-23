// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.account;

import org.springframework.stereotype.Service;

import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelope;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelopes;
import se.digg.wallet.gateway.domain.ports.outbound.AccountPort;

@Service
public class AccountService {

  private final AccountPort accountPort;

  public AccountService(AccountPort accountPort) {
    this.accountPort = accountPort;
  }

  public Account createAccount(NewAccount request) {
    return accountPort.createAccount(request);
  }

  public Account createAccountLegacy(NewAccount request) {
    return accountPort.createAccountLegacy(request);
  }

  public void addAccountWalletKey(Jwk jwk, String accountId) {
    accountPort.addWalletKey(jwk, accountId);
  }

  public void addAccountSecurityEnvelope(SecurityEnvelope securityEnvelope, String accountId) {
    accountPort.addSecurityEnvelope(securityEnvelope, accountId);
  }

  public SecurityEnvelopes getSecurityEnvelopes(String accountId) {
    return accountPort.getSecurityEnvelopes(accountId);
  }

  public String getHsmClientId(String accountId) {
    return accountPort.getHsmClientId(accountId);
  }

  public void saveHsmClientId(String clientId, String accountId) {
    accountPort.saveHsmClientId(clientId, accountId);
  }

}
