// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.AccountApi;
import se.digg.wallet.gateway.api.v0.model.CreateAccountRequestDto;
import se.digg.wallet.gateway.api.v0.model.CreateAccountResponseDto;
import se.digg.wallet.gateway.application.auth.ApiKeyVerifier;
import se.digg.wallet.gateway.application.model.common.JwkDto;
import se.digg.wallet.gateway.domain.service.account.AccountService;

@RestController
public class AccountController implements AccountApi {
  private final AccountService accountService;
  private final ApiKeyVerifier apiKeyVerifier;

  public AccountController(AccountService accountService, ApiKeyVerifier apiKeyVerifier) {
    this.accountService = accountService;
    this.apiKeyVerifier = apiKeyVerifier;
  }

  @Override
  public ResponseEntity<CreateAccountResponseDto> createAccount(
      @Valid CreateAccountRequestDto createAccountRequest) {
    apiKeyVerifier.verify();

    var createAccountRequestDto = toCreateAccountRequestDto(createAccountRequest);
    var createAccountResponseDto = accountService.createAccount(createAccountRequestDto, null);
    var createAccountResponse = toCreateAccountResponse(createAccountResponseDto);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createAccountResponse);
  }

  private static @NonNull CreateAccountResponseDto toCreateAccountResponse(
      se.digg.wallet.gateway.application.model.account.CreateAccountResponseDto createAccountResponseDto) {
    return new CreateAccountResponseDto(
        createAccountResponseDto.accountId());
  }

  private static se.digg.wallet.gateway.application.model.account.@NonNull CreateAccountRequestDto toCreateAccountRequestDto(
      CreateAccountRequestDto createAccountRequest) {
    var jwkRequest = createAccountRequest.getPublicKey();
    var jwkDto = new JwkDto(
        jwkRequest.getKty(),
        jwkRequest.getKid(),
        jwkRequest.getAlg().orElse(null),
        jwkRequest.getUse().orElse(null),
        jwkRequest.getCrv(),
        jwkRequest.getX(),
        jwkRequest.getY());

    return new se.digg.wallet.gateway.application.model.account.CreateAccountRequestDto(
        createAccountRequest.getPersonalIdentityNumber(),
        createAccountRequest.getEmailAdress(),
        createAccountRequest.getTelephoneNumber(),
        jwkDto);
  }
}
