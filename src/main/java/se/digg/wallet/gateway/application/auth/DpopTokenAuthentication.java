// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.DPoPAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

/**
 * Authentication with a DPoP-bound access token from the authorization server, an alternative to
 * the challenge-response session. The token's {@code sub} is the account id and {@code cnf.jkt} the
 * thumbprint of the device key that must sign the DPoP proof of every request.
 */
public final class DpopTokenAuthentication {

  private DpopTokenAuthentication() {}

  /**
   * Accepts access tokens only with the DPoP scheme and a proof; a DPoP-bound token sent as a
   * bearer token is ignored, so the request is not authenticated. Requests without an Authorization
   * header keep the 403 the challenge-response clients get today.
   */
  public static void configure(HttpSecurity http) {
    AuthenticationEntryPoint tokenEntryPoint = new DPoPAuthenticationEntryPoint();
    AuthenticationEntryPoint sessionEntryPoint = new Http403ForbiddenEntryPoint();
    http.oauth2ResourceServer(resourceServer -> resourceServer
        .jwt(Customizer.withDefaults())
        .bearerTokenResolver(request -> null)
        .dPoP(Customizer.withDefaults())
        .authenticationEntryPoint((request, response, exception) -> {
          if (request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
            sessionEntryPoint.commence(request, response, exception);
          } else {
            tokenEntryPoint.commence(request, response, exception);
          }
        }));
  }

  public static boolean isTokenAuthentication(Authentication authentication) {
    return authentication instanceof JwtAuthenticationToken token && token.isAuthenticated();
  }
}
