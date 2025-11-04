// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

import org.springframework.security.core.AuthenticationException;

public class ChallengeResponseAuthenticationException extends AuthenticationException {

  public ChallengeResponseAuthenticationException(String message) {
    super(message);
  }

}
