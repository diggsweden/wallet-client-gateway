// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;

class ChallengeResponseAuthorizationMgrTest {

  private AuthorizationManager<RequestAuthorizationContext> authorizationMgr;

  @BeforeEach
  void setUp() {
    ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
    when(applicationConfig.publicPaths()).thenReturn(List.of("/public/**"));
    SecurityConfig securityConfig = new SecurityConfig(applicationConfig);
    authorizationMgr = securityConfig.challengeResponseAuthorizationMgr();
  }

  @ParameterizedTest
  @ValueSource(strings = {"account-123", "550e8400-e29b-41d4-a716-446655440000"})
  void grantsAccessForValidChallengeResponseWithAccountId(String accountId) {
    var auth = new ChallengeResponseAuthentication(accountId);
    AuthorizationResult decision = authorizationMgr.authorize(asSupplier(auth), null);

    assertThat(decision).isNotNull();
    assertThat(decision.isGranted()).isTrue();
  }

  private static final List<AbstractAuthenticationToken> INVALID_TOKENS = List.of(
      new AnonymousAuthenticationToken("key", "anonymous",
          List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))),
      new UsernamePasswordAuthenticationToken("user", "password"));

  @ParameterizedTest
  @FieldSource("INVALID_TOKENS")
  @NullSource
  void deniesAccessForAnonymousAuthentication(AbstractAuthenticationToken invalidToken) {
    AuthorizationResult decision = authorizationMgr.authorize(asSupplier(invalidToken), null);

    assertThat(decision).isNotNull();
    assertThat(decision.isGranted()).isFalse();
  }

  private Supplier<Authentication> asSupplier(Authentication auth) {
    return () -> auth;
  }
}
