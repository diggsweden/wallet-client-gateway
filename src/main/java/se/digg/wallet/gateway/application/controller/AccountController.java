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
import se.digg.wallet.gateway.api.v0.model.CreateAccountRequestDto;
import se.digg.wallet.gateway.api.v0.model.CreateAccountResponseDto;
import se.digg.wallet.gateway.api.v0.model.KeyRequest;
import se.digg.wallet.gateway.api.v0.model.SecurityEnvelopeRequest;
import se.digg.wallet.gateway.api.v0.model.SecurityEnvelopeResponse;
import se.digg.wallet.gateway.application.auth.ApiKeyVerifier;
import se.digg.wallet.gateway.application.auth.CurrentAccount;
import se.digg.wallet.gateway.application.mapper.account.AccountMapper;
import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelope;
import se.digg.wallet.gateway.domain.service.account.AccountService;

@RestController
public class AccountController implements AccountApi {
  private final AccountService accountService;
  private final ApiKeyVerifier apiKeyVerifier;
  private final AccountMapper mapper;
  private final CurrentAccount currentAccount;

  public AccountController(AccountService accountService,
      ApiKeyVerifier apiKeyVerifier,
      AccountMapper mapper,
      CurrentAccount currentAccount) {
    this.accountService = accountService;
    this.apiKeyVerifier = apiKeyVerifier;
    this.mapper = mapper;
    this.currentAccount = currentAccount;
  }

  @Override
  public ResponseEntity<CreateAccountResponseDto> createAccount(
      @Valid CreateAccountRequestDto createAccountRequest) {
    apiKeyVerifier.verify();

    NewAccount newAccount = mapper.toDomain(createAccountRequest);
    Account account = accountService.createAccount(newAccount);
    CreateAccountResponseDto createAccountResponse = mapper.toResponse(account);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createAccountResponse);
  }

  @Override
  public ResponseEntity<CreateAccountResponseDto> createAccounts(
      @Valid CreateAccountRequest createAccountRequest) {
    apiKeyVerifier.verify();

    NewAccount newAccount = mapper.toDomain(createAccountRequest);
    Account account = accountService.createAccount(newAccount);
    CreateAccountResponseDto createAccountResponse = mapper.toResponse(account);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createAccountResponse);
  }

  @Override
  public ResponseEntity<SecurityEnvelopeResponse> addAccountSecurityEnvelope(
      @Valid SecurityEnvelopeRequest securityEnvelopeRequest) {
    apiKeyVerifier.verify();
    var accountId = currentAccount.id();
    SecurityEnvelope envelope = mapper.toDomain(securityEnvelopeRequest);
    accountService.addAccountSecurityEnvelope(envelope, accountId);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(null);

  }

  @Override
  public ResponseEntity<Void> addAccountWalletKey(@Valid KeyRequest keyRequest) {
    apiKeyVerifier.verify();

    Jwk jwk = mapper.toDomain(keyRequest);
    var accountId = currentAccount.id();
    accountService.addAccountWalletKey(jwk, accountId);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(null);

  }
}
