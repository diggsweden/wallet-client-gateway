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
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.client.RestTestClient;
import se.digg.wallet.gateway.application.config.SessionConfig;
import se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeDto;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeResponseDto;

public class AuthUtil {
  public static final String ACCOUNT_ID = UUID.randomUUID().toString();
  public static final String KEY_ID = "123";


  public static RestTestClient login(WireMockServer wireMockServer, int port,
      RestTestClient restClient)
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
        .body(postBody)
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

  public record Step1Result(
      HttpResponse<Void> response,
      String location,
      Optional<String> sessionId) {
  }
  public record Step2Result(
      HttpResponse<Void> response,
      String location) {
  }
  public record Step3Result(
      String code,
      String nonce,
      String location) {

  }
  public record Step5Result(
      HttpResponse<Void> response) {
  }

  public static Step1Result step1CallProtectedEndpoint(int port)
      throws IOException, InterruptedException {
    var httpClient = HttpClient.newHttpClient();
    final var walletClientGatewayBaseUrl = "http://localhost:" + port;
    var response1 = httpClient.send(
        HttpRequest.newBuilder(URI.create(walletClientGatewayBaseUrl + "/oidc/accounts/v1"))
            .build(),
        BodyHandlers.discarding());
    assertThat(response1.statusCode()).isEqualTo(302);
    var location1 = response1.headers().firstValue("Location").orElseThrow();
    assertThat(location1)
        .isEqualTo(walletClientGatewayBaseUrl + "/oauth2/authorization/iam");
    return new Step1Result(response1,
        location1,
        response1.headers().firstValue("session"));
  }

  public static Step2Result step2CallSpringLoginEndpoint(String uri, String loginSession)
      throws IOException, InterruptedException {
    var httpClient = HttpClient.newHttpClient();
    var response2 = httpClient.send(
        HttpRequest.newBuilder(URI.create(uri))
            .header("session", loginSession)
            .build(),
        BodyHandlers.discarding());
    assertThat(response2.statusCode()).isEqualTo(302);
    var location2 = response2.headers().firstValue("Location").orElseThrow();

    return new Step2Result(response2, location2);
  }

  public static Step3Result step3BuildFakedAsAuthResult(int port,
      String authorizationServerLocation) {
    var code = "CODE1234";
    var state = getQueryParam("state", authorizationServerLocation);
    var nonce = getQueryParam("nonce", authorizationServerLocation);

    var walletClientGatewayBaseUrl = "http://localhost:" + port;
    var redirectUri = walletClientGatewayBaseUrl + "/login/oauth2/code/iam";
    var loggedInResultUri =
        redirectUri + "?code=%s&state=%s&nonce=%s".formatted(code, state, nonce);

    return new Step3Result(code, nonce, loggedInResultUri);
  }

  public static void step4StubAsyncAuthorizationServerCalls(String code,
      String nonce, WireMockServer authorizationServer)
      throws Exception {
    var subject = "testuser";
    var rsaJwk = RSAKey.parse(Files.readString(
        new ClassPathResource("wiremock/authorization_server_key.jwk").getFile().toPath()));
    authorizationServer.stubFor(get(urlEqualTo("/certs"))
        .willReturn(
            okJson("""
                    {
                      "keys": [%s]
                    }
                """
                .formatted(rsaJwk.toPublicJWK().toJSONString()))));

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

  public static Step5Result step5CallLoginEndpointWithCode(String location, String loginSession)
      throws IOException, InterruptedException {
    var httpClient = HttpClient.newHttpClient();

    var response3 = httpClient.send(
        HttpRequest.newBuilder(URI.create(location))
            .header("session", loginSession)
            .build(),
        BodyHandlers.discarding());
    return new Step5Result(response3);
  }

  public static String oauth2Login(int port, WireMockServer authorizationServer)
      throws Exception {
    final var walletClientGatewayBaseUrl = "http://localhost:" + port;
    final var authorizationServerBaseUrl = "http://localhost:" + authorizationServer.port();
    final var redirectUri = walletClientGatewayBaseUrl + "/login/oauth2/code/iam";
    // Call oidc protected endpoint, get redirected to login endpoint
    var step1Result = step1CallProtectedEndpoint(port);
    var loginSession = step1Result.sessionId().orElseThrow();

    // Call login endpoint, get redirected to wiremock /authorize
    var step2Result = step2CallSpringLoginEndpoint(step1Result.location(), loginSession);
    assertThat(step2Result.location())
        .startsWith(authorizationServerBaseUrl)
        .contains(redirectUri);


    // The user logs in and gets redirected back to us with a code, and same state and nonce
    var step3Result = step3BuildFakedAsAuthResult(port, step2Result.location());

    // Prepare the authorization server wiremock that spring will call some endpoints
    step4StubAsyncAuthorizationServerCalls(step3Result.code(), step3Result.nonce(),
        authorizationServer);

    // go to /login/oauth2, get redirected back to original url that we tried to access
    var step5Result = step5CallLoginEndpointWithCode(step3Result.location(), loginSession);
    var location3 = step5Result.response().headers().firstValue("location").orElseThrow();
    assertThat(location3).startsWith(walletClientGatewayBaseUrl + "/oidc/accounts/v1");

    return step5Result.response().headers().firstValue("SESSION").orElseThrow();
  }

  private static ECKey generateKey() throws Exception {
    return new ECKeyGenerator(Curve.P_256)
        .keyID(KEY_ID)
        .algorithm(Algorithm.NONE)
        .keyUse(KeyUse.SIGNATURE)
        .generate();
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
        .claim("scope", "openid https://id.oidc.se/scope/naturalPersonNumber")
        .claim("roles", java.util.List.of("ROLE_USER"))
        .claim("preferred_username", subject)
        .claim("email", subject + "@example.com")
        .claim("nonce", nonce)
        .claim("https://id.oidc.se/claim/personalIdentityNumber", "198001022386")
        .build();

    JWSSigner signer = new RSASSASigner(rsaJwk.toPrivateKey());
    SignedJWT signedJwt = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJwk.getKeyID()).build(), claims);
    signedJwt.sign(signer);
    return signedJwt.serialize();
  }
}
