// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.digg.wallet.gateway.api.v0.AuthenticationApi;
import se.digg.wallet.gateway.api.v0.model.AuthChallengeDto;
import se.digg.wallet.gateway.api.v0.model.AuthChallengeRequest;
import se.digg.wallet.gateway.api.v0.model.AuthChallengeResponse;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;
import se.digg.wallet.gateway.application.model.auth.ValidateAuthChallengeRequestDto;
import se.digg.wallet.gateway.domain.service.auth.AuthService;

@RestController
public class AuthenticationController implements AuthenticationApi {

  private final AuthService authService;

  public AuthenticationController(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public ResponseEntity<AuthChallengeDto> initChallenge(String accountId, String keyId) {
    var challenge = authService.initChallenge(accountId, keyId);
    var response = AuthChallengeDto.builder()
        .nonce(challenge.nonce())
        .build();
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<AuthChallengeResponse> validateChallenge(
      AuthChallengeRequest authChallengeRequest) {
    var challengeRequest = new ValidateAuthChallengeRequestDto(authChallengeRequest.getSignedJwt());
    var validationResult = authService.validateChallenge(challengeRequest);
    if (validationResult.isPresent()) {
      HttpServletRequest req =
          ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
      createAndSaveSession(req, validationResult.orElseThrow());
      String sessionId = req.getSession(false).getId();
      var response = AuthChallengeResponse.builder()
          .sessionId(sessionId)
          .build();
      return ResponseEntity.ok(response);
    }
    return ResponseEntity.status(401).build();
  }

  private void createAndSaveSession(
      HttpServletRequest req, AuthService.ValidationResult validationResult) {
    var session = req.getSession(true);
    var auth = new ChallengeResponseAuthentication(validationResult.accountId());
    var context = SecurityContextHolder.getContext();
    context.setAuthentication(auth);
    session.setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
  }
}
