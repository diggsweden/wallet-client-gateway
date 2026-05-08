// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChallengeResponseAuthorizationMgrTest {

  private AuthorizationManager<RequestAuthorizationContext> authorizationMgr;

  @BeforeEach
  void setUp() {
    ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
    when(applicationConfig.apisecret()).thenReturn("my_secret_key");
    when(applicationConfig.publicPaths()).thenReturn(List.of("/public/**"));
    SecurityConfig securityConfig = new SecurityConfig(applicationConfig);
    authorizationMgr = securityConfig.challengeResponseAuthorizationMgr();
  }

  @Test
  void grantsWhenChallengeResponseAndAuthenticated() {
    // instanceof ChallengeResponseAuthentication == true && isAuthenticated() == true
    var auth = new ChallengeResponseAuthentication("dummyAccountId");
    auth.setAuthenticated(true);

    AuthorizationResult decision = authorizationMgr.authorize(asSupplier(auth), null);

    assertThat(decision).isNotNull();
    assertThat(decision.isGranted()).isTrue();
  }

  @Test
  void deniesWhenChallengeResponseButNotAuthenticated() {
    // instanceof ChallengeResponseAuthentication == true && isAuthenticated() == false
    var auth = new ChallengeResponseAuthentication("dummyAccountId");
    auth.setAuthenticated(false);

    AuthorizationResult decision = authorizationMgr.authorize(asSupplier(auth), null);

    assertThat(decision).isNotNull();
    assertThat(decision.isGranted()).isFalse();
  }

  @Test
  void deniesWhenNotChallengeResponseButAuthenticated() {
    // instanceof ChallengeResponseAuthentication == false && isAuthenticated() == true
    var auth = new TestingAuthenticationToken("dummyAccountId", "credentials");
    auth.setAuthenticated(true);

    AuthorizationResult decision = authorizationMgr.authorize(asSupplier(auth), null);

    assertThat(decision).isNotNull();
    assertThat(decision.isGranted()).isFalse();
  }

  @Test
  void matchesPublicPathWithContextPath() {
    ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
    when(applicationConfig.apisecret()).thenReturn("my_secret_key");
    when(applicationConfig.publicPaths()).thenReturn(List.of("/accounts"));
    var securityConfig = new SecurityConfig(applicationConfig);

    var request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/wallet-client-gateway/accounts");
    when(request.getServletPath()).thenReturn("/accounts");

    assertThat(securityConfig.isPublicPath(request)).isTrue();
  }

  private Supplier<Authentication> asSupplier(Authentication auth) {
    return () -> auth;
  }
}
