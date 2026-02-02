// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.pub;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;
import se.digg.wallet.gateway.application.controller.openapi.auth.ChallengeResponseOpenApiDocumentation;
import se.digg.wallet.gateway.application.controller.openapi.auth.InitChallengeOpenApiDocumentation;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeDto;
import se.digg.wallet.gateway.application.model.auth.ValidateAuthChallengeRequestDto;
import se.digg.wallet.gateway.application.model.auth.ValidateAuthChallengeResponseDto;
import se.digg.wallet.gateway.domain.service.auth.AuthService;
import se.digg.wallet.gateway.domain.service.auth.AuthService.ValidationResult;


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
  public ResponseEntity<ValidateAuthChallengeResponseDto> validateChallenge(
      @RequestBody ValidateAuthChallengeRequestDto response,
      HttpServletRequest req) {
    var validationResult = authService.validateChallenge(response);
    if (validationResult.isPresent()) {
      createAndSaveSession(req, validationResult.orElseThrow());
      String sessionId = req.getSession(false).getId();
      return ResponseEntity.ok(new ValidateAuthChallengeResponseDto(sessionId));
    }
    return ResponseEntity.status(401).build();
  }

  private void createAndSaveSession(HttpServletRequest req, ValidationResult validationResult) {
    var session = req.getSession(true);
    var auth = new ChallengeResponseAuthentication(validationResult.accountId());
    var context = SecurityContextHolder.getContext();
    context.setAuthentication(auth);
    session.setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        context);
  }

}
