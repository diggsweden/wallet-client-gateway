// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.digg.wallet.gateway.application.auth.OidcClaims;
import se.digg.wallet.gateway.application.config.SessionConfig;
import se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeDto;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeResponseDto;

public class AuthUtil {
  public static final String ACCOUNT_ID = UUID.randomUUID().toString();
  public static final String KEY_ID = "123";


  @SuppressWarnings("null")
  public static WebTestClient login(WireMockServer wireMockServer, int port,
      WebTestClient restClient)
      throws Exception {
    var generatedKeyPair = generateKey();

    wireMockServer.stubFor(get("/account/" + ACCOUNT_ID)
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
                CreateAccountRequestDtoTestBuilder.PERSONAL_IDENTITY_NUMBER,
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

  public static WebTestClient oauth2Login(int port, WireMockServer authorizationServer,
      WebTestClient restClient)
      throws Exception {
    final var walletClientGatewayBaseUrl = "http://localhost:" + port;
    final var authorizationServerBaseUrl = "http://localhost:" + authorizationServer.port();
    final var redirectUri = walletClientGatewayBaseUrl + "/login/oauth2/code/myprovider";

    var httpClient = HttpClient.newBuilder()
        .build();

    // Call oidc protected endpoint, get redirected to login endpoint
    var response1 = httpClient.send(
        HttpRequest.newBuilder(URI.create(walletClientGatewayBaseUrl + "/oidc/accounts/v1"))
            .build(),
        BodyHandlers.discarding());
    assertThat(response1.statusCode()).isEqualTo(302);
    var location1 = response1.headers().firstValue("Location").orElseThrow();
    assertThat(location1)
        .isEqualTo(walletClientGatewayBaseUrl + "/oauth2/authorization/myprovider");
    var session = response1.headers().firstValue("session").orElseThrow();

    // Call login endpoint, get redirected to wiremock /authorize
    var response2 = httpClient.send(
        HttpRequest.newBuilder(URI.create(location1))
            .header("session", session)
            .build(),
        BodyHandlers.discarding());
    assertThat(response2.statusCode()).isEqualTo(302);
    var location2 = response2.headers().firstValue("Location").orElseThrow();

    assertThat(location2)
        .startsWith(authorizationServerBaseUrl)
        .contains(redirectUri);


    // The user logs in and gets redirected back to us with a code, and same state and nonce
    var code = "CODE1234";
    var state = getQueryParam("state", location2);
    var nonce = getQueryParam("nonce", location2);
    var loggedInResultUri =
        redirectUri + "?code=%s&state=%s&nonce=%s".formatted(code, state, nonce);

    var subject = "testuser";
    var rsaJwk = RSAKey.parse(Files.readString(
        new ClassPathResource("wiremock/authorization_server_key.jwk").getFile().toPath()));
    stubAsyncAuthorizationServerCalls(code, subject, location2, authorizationServer, rsaJwk);

    // go to /login/oauth2, get redirected back to original url that we tried to access
    var response3 = httpClient.send(
        HttpRequest.newBuilder(URI.create(loggedInResultUri))
            .header("session", session)
            .build(),
        BodyHandlers.ofString());
    var location3 = response3.headers().firstValue("location").orElseThrow();
    assertThat(location3).startsWith(walletClientGatewayBaseUrl + "/oidc/accounts/v1");

    return restClient.mutate()
        .defaultHeader(SessionConfig.SESSION_HEADER,
            response3.headers().firstValue(SessionConfig.SESSION_HEADER).orElseThrow())
        .build();
  }

  private static ECKey generateKey() throws Exception {
    return new ECKeyGenerator(Curve.P_256)
        .keyID(KEY_ID)
        .algorithm(Algorithm.NONE)
        .keyUse(KeyUse.SIGNATURE)
        .generate();
  }



  private static void stubAsyncAuthorizationServerCalls(String code, String subject,
      String location2, WireMockServer authorizationServer, RSAKey rsaJwk)
      throws Exception {
    authorizationServer.stubFor(get(urlEqualTo("/jwks"))
        .willReturn(
            okJson("""
                    {
                      "keys": [%s]
                    }
                """
                .formatted(rsaJwk.toPublicJWK().toJSONString()))));

    var nonce = getQueryParam("nonce", location2);
    String idToken =
        createSignedJwt(subject, "http://localhost:" + authorizationServer.port(),
            "localhost-test-client", 300,
            nonce, rsaJwk);
    String accessToken = "at-" + System.currentTimeMillis();
    String tokenResponseJson = String.format("""
        {
          "access_token":"%s",
          "id_token":"%s",
          "token_type":"Bearer",
          "expires_in":300
        }
        """, accessToken, idToken);

    authorizationServer.stubFor(post(urlEqualTo("/token"))
        .withRequestBody(containing("code=" + code))
        .willReturn(okJson(tokenResponseJson)));

    authorizationServer.stubFor(get(urlEqualTo("/userinfo"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                  "sub": "%s",
                  "preferred_username": "test-user",
                  "name": "Test User",
                  "given_name": "Test",
                  "family_name": "User",
                  "email": "test.user@example.com",
                  "email_verified": true
                }
                """.formatted(subject))));
  }

  private static String getQueryParam(String param, String location2) {
    var fromParam = location2.substring(location2.indexOf(param + "=") + param.length() + 1);
    return fromParam.contains("&")
        ? fromParam.substring(0, Math.min(fromParam.length(), fromParam.indexOf("&")))
        : fromParam;
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

    signedJwt.sign(signer);

    return signedJwt.serialize();
  }


  static String createSignedJwt(String subject, String issuer, String aud, long lifetimeSeconds,
      String nonce, RSAKey rsaJwk)
      throws Exception {
    Instant now = Instant.now();
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject(subject)
        .issuer(issuer)
        .audience(aud)
        .expirationTime(Date.from(now.plusSeconds(lifetimeSeconds)))
        .issueTime(Date.from(now))
        .claim("scope", "openid %s".formatted(OidcClaims.PERSONAL_IDENTITY_NUMBER_CLAIM.key()))
        .claim("roles", java.util.List.of("ROLE_USER"))
        .claim("preferred_username", subject)
        .claim("email", subject + "@example.com")
        .claim("nonce", nonce)
        .claim(OidcClaims.PERSONAL_IDENTITY_NUMBER_CLAIM.key(), "198001022386")
        .build();

    JWSSigner signer = new RSASSASigner(rsaJwk.toPrivateKey());
    SignedJWT signedJwt = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJwk.getKeyID()).build(), claims);
    signedJwt.sign(signer);
    return signedJwt.serialize();
  }
}
