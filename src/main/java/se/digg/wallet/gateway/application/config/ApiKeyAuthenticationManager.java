// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyAuthenticationManager implements AuthenticationManager {

  private final ApplicationConfig applicationConfig;

  public ApiKeyAuthenticationManager(ApplicationConfig applicationConfig) {
    this.applicationConfig = applicationConfig;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String principal = (String) authentication.getPrincipal();
    if (!applicationConfig.apisecret().equals(principal)) {
      throw new BadCredentialsException("The API key was not found or not the expected value.");
    }
    authentication.setAuthenticated(true);
    return authentication;
  }
}
