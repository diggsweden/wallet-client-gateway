// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.auth.ApiKeyVerifier;
import se.digg.wallet.gateway.application.controller.exception.BadRequestException;
import se.digg.wallet.gateway.application.controller.openapi.account.PostOpenApiDocumentation;
import se.digg.wallet.gateway.application.model.account.CreateAccountRequestDto;
import se.digg.wallet.gateway.application.model.account.CreateAccountResponseDto;
import se.digg.wallet.gateway.domain.service.account.AccountService;

@RestController
@RequestMapping({"/accounts", "/accounts/v1"})
public class AccountController {
  private final AccountService accountService;
  private final ApiKeyVerifier apiKeyVerifier;

  public AccountController(AccountService accountService, ApiKeyVerifier apiKeyVerifier) {
    this.accountService = accountService;
    this.apiKeyVerifier = apiKeyVerifier;
  }

  @PostMapping
  @PostOpenApiDocumentation
  public ResponseEntity<CreateAccountResponseDto> createAccount(
      @RequestBody @Valid CreateAccountRequestDto requestDto) {
    apiKeyVerifier.verify();
    if (requestDto.publicKey().kid() == null || requestDto.publicKey().kid().isEmpty()) {
      throw new BadRequestException("publicKey.kid is required when creating account");
    }
    var responseDto = accountService.createAccount(requestDto, null);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(responseDto);
  }
}
