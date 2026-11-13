// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.AccountApi;
import se.digg.wallet.gateway.api.v0.model.CreateAccountRequest;
import se.digg.wallet.gateway.api.v0.model.CreateAccountResponse;
import se.digg.wallet.gateway.application.auth.CurrentAccount;
import se.digg.wallet.gateway.application.mapper.account.AccountMapper;
import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.model.account.WalletPut;
import se.digg.wallet.gateway.domain.service.AccountService;

@RestController
public class AccountController implements AccountApi {
  private final AccountService accountService;
  private final AccountMapper mapper;
  private final CurrentAccount currentAccount;

  public AccountController(AccountService accountService, AccountMapper mapper,
      CurrentAccount currentAccount) {
    this.accountService = accountService;
    this.mapper = mapper;
    this.currentAccount = currentAccount;
  }

  /**
   * Only with a DPoP-bound access token: the wallet is put under the token's account, with the
   * device key the token is bound to.
   */
  @Override
  public ResponseEntity<CreateAccountResponse> putWallet(
      @Valid CreateAccountRequest createAccountRequest) {
    var boundKey = currentAccount.boundKeyThumbprint();
    if (boundKey.isEmpty()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    var requested = createAccountRequest.getDeviceKey();
    if (!boundKey.equals(mapper.computeThumbprint(requested))) {
      return ResponseEntity.badRequest().build();
    }

    WalletPut result = accountService.putWallet(
        UUID.fromString(currentAccount.id()), mapper.toDomain(requested));

    return ResponseEntity
        .status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
        .body(mapper.toResponse(result.account()));
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
