// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import static java.util.function.Predicate.not;

import com.nimbusds.jose.jwk.JWK;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.NimbusJwtClientAuthenticationParametersConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.RestClientRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;
import se.swedenconnect.security.credential.PkiCredential;
import se.swedenconnect.security.credential.bundle.CredentialBundles;
import se.swedenconnect.security.credential.nimbus.JwkTransformerFunction;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  public static final String API_KEY_HEADER = "X-API-KEY";

  private final List<String> publicPaths;
  private final Optional<String> privateJwtAudience;

  public SecurityConfig(
      ApplicationConfig applicationConfig) {
    this.publicPaths = applicationConfig.publicPaths();
    this.privateJwtAudience = applicationConfig
        .authorizationServer()
        .privateJwtAudience()
        .filter(not(String::isBlank));
  }

  @Bean
  @Order(1)
  public SecurityFilterChain oidcSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .securityMatcher("/oidc/**", "/oauth2/**", "/login/oauth2/**")
        .authorizeHttpRequests(a -> a
            .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
            .anyRequest()
            .access(oidcAuthorizationManager()))
        .oauth2Login(Customizer.withDefaults());

    return httpSecurity.build();
  }


  @Bean
  @Order(2)
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity)
      throws Exception {
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
  AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
    return cfg.getAuthenticationManager();
  }

  /**
   * Same as normal "authenticated" except it also checks that it's a valid
   * OAuth2AuthenticationToken.
   */
  private AuthorizationManager<RequestAuthorizationContext> oidcAuthorizationManager() {
    var defaultAuthenticationManager = AuthenticatedAuthorizationManager.authenticated();
    return (authentication, context) -> new AuthorizationDecision(
        defaultAuthenticationManager.authorize(authentication, context).isGranted()
            && authentication.get() instanceof OAuth2AuthenticationToken);
  }

  /**
   * Same as normal "authenticated" except it also checks that it's a valid
   * ChallengeResponseAuthentication.
   */
  private AuthorizationManager<RequestAuthorizationContext> challengeResponseAuthorizationMgr() {
    var defaultAuthenticationManager = AuthenticatedAuthorizationManager.authenticated();
    return (authentication, context) -> new AuthorizationDecision(
        defaultAuthenticationManager.authorize(authentication, context).isGranted()
            && authentication.get() instanceof ChallengeResponseAuthentication);
  }

  private Function<ClientRegistration, JWK> parameterConverter(
      final CredentialBundles credentialBundles) {
    return (clientRegistration) -> {
      if (ClientAuthenticationMethod.PRIVATE_KEY_JWT
          .equals(clientRegistration.getClientAuthenticationMethod())) {
        final String privateJwtKeyAlias = clientRegistration.getRegistrationId();

        final PkiCredential keyPair = credentialBundles.getCredential(privateJwtKeyAlias);
        if (keyPair == null) {
          throw new IllegalStateException(
              "No private key found for privateJwtKeyAlias " + privateJwtKeyAlias);
        }
        final JwkTransformerFunction function = new JwkTransformerFunction();
        function.setKeyIdFunction(pkiCredential -> createKeyId(pkiCredential.getPublicKey()));
        final JWK jwk = function.apply(keyPair);

        return jwk.toRSAKey();
      }
      return null;
    };
  }

  @Bean
  RestClientAuthorizationCodeTokenResponseClient codeTokenResponseClient(
      final CredentialBundles credentialBundles) {
    final var tokenResponseClient =
        new RestClientAuthorizationCodeTokenResponseClient();

    final var parametersConverter =
        new NimbusJwtClientAuthenticationParametersConverter<OAuth2AuthorizationCodeGrantRequest>(
            this.parameterConverter(credentialBundles));

    privateJwtAudience
        .ifPresent(aud -> parametersConverter.setJwtClientAssertionCustomizer(context -> {
          context.getClaims()
              .audience(List.of(aud));
        }));
    tokenResponseClient.setParametersConverter(parametersConverter);

    return tokenResponseClient;
  }

  @Bean
  public OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> accessTokenResponseClient(
      final CredentialBundles credentialBundles) {

    final RestClientRefreshTokenTokenResponseClient refreshTokenResponseClient =
        new RestClientRefreshTokenTokenResponseClient();
    refreshTokenResponseClient.setParametersConverter(
        new NimbusJwtClientAuthenticationParametersConverter<>(
            this.parameterConverter(credentialBundles)));
    return refreshTokenResponseClient;
  }

  /**
   * Generates a unique key identifier based on the SHA-256 hash of the input key's encoded form.
   * This is the same algoritm used in keycloak, when generating a kid for the public key.
   *
   * @param key the cryptographic key for which the unique identifier is to be generated
   * @return a URL-safe Base64 encoded string representation of the SHA-256 hash of the key
   * @throws RuntimeException if the SHA-256 algorithm is not available
   */
  private static String createKeyId(final Key key) {
    try {
      final byte[] sha256Digest = MessageDigest.getInstance("SHA-256").digest(key.getEncoded());
      return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256Digest);
    } catch (final NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }


}
