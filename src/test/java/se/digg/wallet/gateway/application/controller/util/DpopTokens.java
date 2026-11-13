// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * DPoP-bound access tokens as the authorization server issues them, and DPoP proofs as the app
 * signs them with its device key. Test classes using it point the resource server at the JWKS
 * served by {@link #stubJwks} and expect {@link #ISSUER} and {@link #AUDIENCE}.
 */
public final class DpopTokens {

  public static final String ISSUER = "http://authorization-server.test";
  public static final String AUDIENCE = "wallet-app";

  private DpopTokens() {}

  public static ECKey newKey(String kid) {
    try {
      return new ECKeyGenerator(Curve.P_256).keyID(kid).generate();
    } catch (JOSEException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static void stubJwks(WireMockServer authorizationServer, ECKey signingKey) {
    authorizationServer.stubFor(get("/oauth2/jwks").willReturn(aResponse()
        .withHeader("content-type", "application/json")
        .withBody(new JWKSet(signingKey.toPublicJWK()).toString())));
  }

  /** An access token for the account, bound to the device key. */
  public static String accessToken(ECKey issuerKey, String accountId, ECKey deviceKey) {
    return accessToken(issuerKey, ISSUER, accountId, deviceKey);
  }

  public static String accessToken(ECKey issuerKey, String issuer, String accountId,
      ECKey deviceKey) {
    return accessToken(issuerKey, issuer, AUDIENCE, accountId, deviceKey);
  }

  public static String accessToken(ECKey issuerKey, String issuer, String audience,
      String accountId, ECKey deviceKey) {
    try {
      Instant now = Instant.now();
      var claims = new JWTClaimsSet.Builder()
          .issuer(issuer)
          .subject(accountId)
          .audience(audience)
          .issueTime(Date.from(now))
          .expirationTime(Date.from(now.plusSeconds(300)))
          .jwtID(UUID.randomUUID().toString())
          .claim("scope", "wallet")
          .claim("cnf", Map.of("jkt", deviceKey.computeThumbprint().toString()))
          .build();
      var jwt = new SignedJWT(
          new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(issuerKey.getKeyID()).build(), claims);
      jwt.sign(new ECDSASigner(issuerKey));
      return jwt.serialize();
    } catch (JOSEException ex) {
      throw new IllegalStateException(ex);
    }
  }

  /** A DPoP proof for one request with the access token, signed by the key. */
  public static String proof(ECKey key, String method, String url, String accessToken) {
    try {
      byte[] hash = MessageDigest.getInstance("SHA-256")
          .digest(accessToken.getBytes(StandardCharsets.US_ASCII));
      var claims = new JWTClaimsSet.Builder()
          .claim("htm", method)
          .claim("htu", url)
          .claim("ath", Base64URL.encode(hash).toString())
          .issueTime(new Date())
          .jwtID(UUID.randomUUID().toString())
          .build();
      var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256)
          .type(new JOSEObjectType("dpop+jwt"))
          .jwk(key.toPublicJWK())
          .build(), claims);
      jwt.sign(new ECDSASigner(key));
      return jwt.serialize();
    } catch (JOSEException | NoSuchAlgorithmException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
