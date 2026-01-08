// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

import java.io.Serial;
import java.util.List;
import java.util.Objects;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class ChallengeResponseAuthentication extends AbstractAuthenticationToken {

  @Serial
  private static final long serialVersionUID = 1L;

  private final String accountId;

  public ChallengeResponseAuthentication(String accountId) {
    super(List.of());
    this.accountId = Objects.requireNonNull(accountId);
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return accountId;
  }

  @Override
  public Object getPrincipal() {
    return accountId;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ChallengeResponseAuthentication
        && ChallengeResponseAuthentication.class.cast(obj).accountId
            .equals(accountId);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + Objects.hashCode(accountId);
  }

}
