// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.pub;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.controller.openapi.auth.ChallengeResponseOpenApiDocumentation;
import se.digg.wallet.gateway.application.controller.openapi.auth.InitChallengeOpenApiDocumentation;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeDto;
import se.digg.wallet.gateway.domain.service.auth.AuthService;


@RestController
@RequestMapping("/public/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/session/challenge")
  @InitChallengeOpenApiDocumentation
  public ResponseEntity<AuthChallengeDto> initChallenge(
      @RequestParam @NotBlank String accountId,
      @RequestParam @NotBlank String keyId) {
    var challenge = authService.initChallenge(accountId, keyId);
    return ResponseEntity.ok(challenge);
  }

  @PostMapping("/session/response")
  @ChallengeResponseOpenApiDocumentation
  public ResponseEntity<Void> validateChallenge() {
    return ResponseEntity.ok().build();
  }

}
