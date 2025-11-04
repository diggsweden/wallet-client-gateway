// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthenticationConverter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  public static final String API_KEY_HEADER = "X-API-KEY";

  private final ChallengeResponseAuthenticationConverter authenticationConverter;
  private final List<String> publicPaths;
  private final String challengeResponseEndpoint;

  public SecurityConfig(ChallengeResponseAuthenticationConverter authenticationConverter,
      ApplicationConfig applicationConfig) {
    this.authenticationConverter = authenticationConverter;
    this.publicPaths = applicationConfig.publicPaths();
    this.challengeResponseEndpoint = applicationConfig.challengeResponseEndpoint();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
      AuthenticationManager authManager) throws Exception {
    var loginFilter = createLoginFilter(authManager);

    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests((authorize) -> authorize
            .requestMatchers(publicPaths.toArray(String[]::new))
            .permitAll()
            .anyRequest().authenticated())
        .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
        .sessionManagement(
            (session) -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

    return httpSecurity.build();
  }

  private AuthenticationFilter createLoginFilter(AuthenticationManager authManager) {
    var loginFilter = new AuthenticationFilter(authManager, authenticationConverter);
    loginFilter.setRequestMatcher(PathPatternRequestMatcher.withDefaults()
        .matcher(HttpMethod.POST, challengeResponseEndpoint));
    loginFilter.setSuccessHandler((req, res, auth) -> {
      req.getSession(true).setAttribute("accountId", auth.getName());
      req.changeSessionId(); // rotate id
      res.setStatus(204);
    });
    loginFilter.setFailureHandler((req, res, ex) -> res.sendError(401, "Unauthorized"));
    return loginFilter;
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
    return cfg.getAuthenticationManager();
  }

}
