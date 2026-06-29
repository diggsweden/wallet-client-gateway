// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
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
    throw new AccessDeniedException("no authenticated account");
  }
}
