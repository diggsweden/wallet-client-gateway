// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.domain.service.auth.AuthService;

@Component
public class ChallengeResponseAuthenticationProvider implements AuthenticationProvider {

  private final AuthService authService;

  public ChallengeResponseAuthenticationProvider(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public Authentication authenticate(Authentication authentication)
      throws AuthenticationException {
    var challengeResponseAuthentication =
        ChallengeResponseAuthenticationToken.class.cast(authentication);
    var challengeResponse = challengeResponseAuthentication.getChallengeResponse();

    var validated = authService.validateChallenge(challengeResponse);
    if (validated.isPresent()) {
      return challengeResponseAuthentication.authenticate(validated.get().accountId());
    }
    throw new ChallengeResponseAuthenticationException("Could not validate challenge");
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return ChallengeResponseAuthenticationToken.class.equals(authentication);
  }
}
