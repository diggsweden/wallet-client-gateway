// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.auth.OidcClaims;
import se.digg.wallet.gateway.application.controller.exception.BadRequestException;
import se.digg.wallet.gateway.application.controller.openapi.account.PostOpenApiDocumentation;
import se.digg.wallet.gateway.application.model.account.CreateAccountRequestDto;
import se.digg.wallet.gateway.application.model.account.CreateAccountResponseDto;
import se.digg.wallet.gateway.domain.service.account.AccountService;

@RestController
@RequestMapping("/oidc/accounts/v1")
public class AccountControllerV2 {
  private final AccountService accountService;

  public AccountControllerV2(AccountService accountService) {
    this.accountService = accountService;
  }

  @PostMapping
  @PostOpenApiDocumentation
  public ResponseEntity<CreateAccountResponseDto> createAccount(
      @RequestBody @Valid CreateAccountRequestDto requestDto,
      @AuthenticationPrincipal OidcUser oidcUser,
      HttpSession oidcSession) {
    if (requestDto.publicKey().kid() == null || requestDto.publicKey().kid().isEmpty()) {
      throw new BadRequestException("publicKey.kid is required when creating account");
    }
    var personalIdentityNumber =
        oidcUser.getClaimAsString(OidcClaims.PERSONAL_IDENTITY_NUMBER_CLAIM.key());
    if (personalIdentityNumber == null || personalIdentityNumber.length() != 12) {
      throw new BadRequestException("user does not have a valid personal identity number");
    }
    var responseDto = accountService.createAccount(requestDto, personalIdentityNumber);

    oidcSession.invalidate();

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(responseDto);
  }
}
