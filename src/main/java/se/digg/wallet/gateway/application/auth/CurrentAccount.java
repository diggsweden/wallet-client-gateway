// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

import java.util.Map;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class CurrentAccount {
  public String id() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof ChallengeResponseAuthentication a) {
      return a.getAccountId();
    }
    if (DpopTokenAuthentication.isTokenAuthentication(auth)) {
      return auth.getName();
    }
    throw new AccessDeniedException("no authenticated account");
  }

  /**
   * The thumbprint of the device key the access token is bound to, read from its {@code cnf.jkt}
   * claim; only a DPoP-bound access token has one.
   */
  public Optional<String> boundKeyThumbprint() {
    if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken t
        && t.isAuthenticated()
        && t.getToken().getClaims().get("cnf") instanceof Map<?, ?> cnf
        && cnf.get("jkt") instanceof String jkt) {
      return Optional.of(jkt);
    }
    return Optional.empty();
  }
}
