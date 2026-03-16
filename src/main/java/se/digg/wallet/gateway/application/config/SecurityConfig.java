// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  public static final String API_KEY_HEADER = "X-API-KEY";

  private final List<String> publicPaths;

  public SecurityConfig(
      ApplicationConfig applicationConfig) {
    this.publicPaths = applicationConfig.publicPaths();
  }

  @Bean
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity) {
    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests((authorize) -> authorize
            .requestMatchers(publicPaths.toArray(String[]::new)).permitAll()
            .anyRequest()
            .access(challengeResponseAuthorizationMgr()));

    return httpSecurity.build();
  }

  // Disables auto config of user repository
  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) {
    return cfg.getAuthenticationManager();
  }

  /**
   * Same as normal "authenticated" except it also checks that it's a valid
   * ChallengeResponseAuthentication.
   */
  AuthorizationManager<RequestAuthorizationContext> challengeResponseAuthorizationMgr() {
    var defaultAuthenticationManager = AuthenticatedAuthorizationManager.authenticated();
    return (authentication, context) -> new AuthorizationDecision(
        checkIfGranted(defaultAuthenticationManager.authorize(authentication, context))
            && authentication.get() instanceof ChallengeResponseAuthentication);
  }

  private boolean checkIfGranted(AuthorizationResult authorizationResult) {
    return authorizationResult != null && authorizationResult.isGranted();
  }
}
