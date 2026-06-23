// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.AccountApi;
import se.digg.wallet.gateway.api.v0.model.CreateAccountRequest;
import se.digg.wallet.gateway.api.v0.model.CreateAccountResponse;
import se.digg.wallet.gateway.api.v0.model.EcJwkRequest;
import se.digg.wallet.gateway.api.v0.model.SecurityEnvelopesResponse;
import se.digg.wallet.gateway.application.auth.CurrentAccount;
import se.digg.wallet.gateway.application.mapper.account.AccountMapper;
import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelopes;
import se.digg.wallet.gateway.domain.service.account.AccountService;

@RestController
public class AccountController implements AccountApi {
  private final AccountService accountService;
  private final AccountMapper mapper;
  private final CurrentAccount currentAccount;

  public AccountController(AccountService accountService,
      AccountMapper mapper,
      CurrentAccount currentAccount) {
    this.accountService = accountService;
    this.mapper = mapper;
    this.currentAccount = currentAccount;
  }

  @Override
  public ResponseEntity<CreateAccountResponse> createAccount(
      @Valid CreateAccountRequest createAccountRequest) {
    NewAccount newAccount = mapper.toDomain(createAccountRequest);
    Account account = accountService.createAccount(newAccount);
    CreateAccountResponse createAccountResponse = mapper.toResponse(account);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createAccountResponse);
  }

  @Override
  public ResponseEntity<SecurityEnvelopesResponse> getAccountSecurityEnvelopes() {
    var accountId = currentAccount.id();
    SecurityEnvelopes envelopes = accountService.getSecurityEnvelopes(accountId);
    return ResponseEntity.ok(mapper.toResponse(envelopes));
  }

  @Override
  public ResponseEntity<Void> addAccountWalletKey(@Valid EcJwkRequest keyRequest) {
    Jwk jwk = mapper.toDomain(keyRequest);
    var accountId = currentAccount.id();
    accountService.addAccountWalletKey(jwk, accountId);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .build();
  }
}
