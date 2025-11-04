// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

import java.util.List;
import java.util.Objects;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeResponseDto;

public class ChallengeResponseAuthenticationToken extends AbstractAuthenticationToken {

  private final AuthChallengeResponseDto challengeResponse;
  private final String accountId;

  public ChallengeResponseAuthenticationToken(AuthChallengeResponseDto challengeResponse,
      String accountId) {
    super(List.of());
    this.challengeResponse = Objects.requireNonNull(challengeResponse);
    this.accountId = accountId;
  }

  @Override
  public Object getCredentials() {
    return challengeResponse;
  }

  public AuthChallengeResponseDto getChallengeResponse() {
    return challengeResponse;
  }

  @Override
  public Object getPrincipal() {
    return accountId != null ? accountId : "FAKE_PRINCIPAL";
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ChallengeResponseAuthenticationToken
        && ChallengeResponseAuthenticationToken.class.cast(obj).challengeResponse
            .equals(challengeResponse);
  }

  @Override
  public int hashCode() {
    return Objects.hash(challengeResponse, accountId);
  }

  public ChallengeResponseAuthenticationToken authenticate(String accountId) {
    var token = new ChallengeResponseAuthenticationToken(challengeResponse, accountId);
    token.setAuthenticated(true);
    return token;
  }
}
