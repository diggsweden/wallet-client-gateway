// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.digg.wallet.gateway.application.config.SessionConfig;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeDto;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeResponseDto;

public class AuthUtil {
  public static final String ACCOUNT_ID = UUID.randomUUID().toString();
  public static final String KEY_ID = "123";

  @SuppressWarnings("null")
  public static WebTestClient login(int port, WebTestClient restClient)
      throws Exception {
    var generatedKeyPair = generateKey();

    stubFor(get("/account/" + ACCOUNT_ID)
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "id": "%s",
                  "personalIdentityNumber": "%s",
                  "emailAdress": "%s",
                  "telephoneNumber": "%s",
                  "publicKey": {
                    "kty": "%s",
                    "kid": "%s",
                    "alg": "%s",
                    "use": "%s",
                    "crv": "%s",
                    "x": "%s",
                    "y": "%s"
                    }
                }
                """.formatted(
                ACCOUNT_ID,
                "1990",
                "a@b.c",
                "007 007",
                generatedKeyPair.getKeyType().getValue(),
                generatedKeyPair.getKeyID(),
                generatedKeyPair.getAlgorithm().toString(),
                generatedKeyPair.getKeyUse(),
                generatedKeyPair.getCurve().toString(),
                generatedKeyPair.getX().toString(),
                generatedKeyPair.getY().toString()))));

    var challenge = restClient.get()
        .uri("http://localhost:%s/public/auth/session/challenge?accountId=%s&keyId=%s"
            .formatted(port, ACCOUNT_ID, KEY_ID))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(AuthChallengeDto.class)
        .returnResult()
        .getResponseBody();

    var signedJwt = createSignedJwt(generatedKeyPair, challenge.nonce());
    var postBody = new AuthChallengeResponseDto(signedJwt);

    var sessionId = new AtomicReference<String>();
    restClient.post()
        .uri("http://localhost:%s/public/auth/session/response".formatted(port))
        .bodyValue(postBody)
        .header("content-type", "application/json")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectHeader()
        .value(SessionConfig.SESSION_HEADER, sessionId::set);

    return restClient.mutate()
        .defaultHeader(SessionConfig.SESSION_HEADER, sessionId.get())
        .build();
  }

  private static ECKey generateKey() throws Exception {
    return new ECKeyGenerator(Curve.P_256)
        .keyID(KEY_ID)
        .algorithm(Algorithm.NONE)
        .keyUse(KeyUse.SIGNATURE)
        .generate();
  }

  private static String createSignedJwt(ECKey ecJwk, String nonce) throws JOSEException {
    JWSSigner signer = new ECDSASigner(ecJwk);
    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .claim("accountId", ACCOUNT_ID)
        .claim("nonce", nonce)
        .expirationTime(new Date(new Date().getTime() + 60 * 1000))
        .build();
    SignedJWT signedJwt = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(ecJwk.getKeyID()).build(),
        claimsSet);

    // Compute the EC signature
    signedJwt.sign(signer);

    // Serialize the JWS to compact form
    return signedJwt.serialize();
  }

}
