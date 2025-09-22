// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired
  private ApiKeyAuthenticationManager apiKeyAuthenticationManager;

  @Autowired
  private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter();
    filter.setAuthenticationManager(apiKeyAuthenticationManager);

    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .addFilter(filter)
        .authorizeHttpRequests((authorize) -> authorize
            .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui.html",
                "/swagger-ui/**")
            .permitAll()
            .anyRequest().authenticated())
        .exceptionHandling(
            customizer -> customizer.authenticationEntryPoint(customAuthenticationEntryPoint))
        .sessionManagement(
            (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return httpSecurity.build();
  }
}
