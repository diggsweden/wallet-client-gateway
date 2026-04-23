// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.account.client;

import java.util.UUID;

import org.springframework.stereotype.Component;

import se.digg.wallet.gateway.client.account.origin.api.AccountControllerApi;
import se.digg.wallet.gateway.client.account.origin.model.AccountDto;
import se.digg.wallet.gateway.client.account.v0.api.AccountApi;
import se.digg.wallet.gateway.client.account.v0.model.AccountResponse;
import se.digg.wallet.gateway.client.account.v0.model.KeyRequest;
import se.digg.wallet.gateway.client.account.v0.model.SecurityEnvelopeRequest;
import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelope;
import se.digg.wallet.gateway.domain.ports.outbound.AccountPort;
import se.digg.wallet.gateway.infrastructure.account.mapper.AccountClientMapper;

@Component
public class WalletAccountAdapter implements AccountPort {

  private final AccountApi accountApi;
  private final AccountControllerApi originAccountApi;
  private final AccountClientMapper accountClientMapper;

  public WalletAccountAdapter(
      AccountApi accountApi,
      AccountControllerApi originAccountApi,
      AccountClientMapper accountClientMapper) {
    this.accountApi = accountApi;
    this.originAccountApi = originAccountApi;
    this.accountClientMapper = accountClientMapper;
  }

  @Override
  public Account createAccount(NewAccount newAccount) {
    AccountResponse response =
        accountApi.createAccount(accountClientMapper.toClientRequest(newAccount));
    return accountClientMapper.toDomain(response);
  }

  @Override
  public Account createAccountLegacy(NewAccount newAccount) {
    AccountDto response =
        originAccountApi.createAccount(accountClientMapper.toOriginClientRequest(newAccount));
    return accountClientMapper.toDomain(response);
  }

  @Override
  public void addWalletKey(Jwk walletKey, String accountId) {
    KeyRequest keyRequest = accountClientMapper.toClientRequest(walletKey);
    UUID id = UUID.fromString(accountId);
    accountApi.addAccountWalletKey(id, keyRequest);
  }

  @Override
  public void addSecurityEnvelope(SecurityEnvelope securityEnvelope, String accountId) {
    UUID id = UUID.fromString(accountId);
    SecurityEnvelopeRequest request = accountClientMapper.toClientRequest(securityEnvelope);
    accountApi.addAccountSecurityEnvelope(id, request);
  }

}
