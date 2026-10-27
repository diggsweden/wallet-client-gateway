// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
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
import se.digg.wallet.gateway.application.mapper.account.AccountMapper;
import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.service.AccountService;

@RestController
public class AccountController implements AccountApi {
  private final AccountService accountService;
  private final AccountMapper mapper;

  public AccountController(AccountService accountService, AccountMapper mapper) {
    this.accountService = accountService;
    this.mapper = mapper;
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
}
