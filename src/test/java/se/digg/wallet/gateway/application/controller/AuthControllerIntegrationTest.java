// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
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
import com.redis.testcontainers.RedisContainer;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.application.config.SessionConfig;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeDto;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeResponseDto;
import se.digg.wallet.gateway.infrastructure.auth.cache.ChallengeCache;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@WalletAccountMock
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  private static final String ACCOUNT_ID = UUID.randomUUID().toString();
  private static final String KEY_ID = "123";

  @Autowired
  private ChallengeCache challengeCache;

  @Autowired
  private WebTestClient restClient;

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer server;


  @SuppressWarnings("null")
  @Test
  void testInitStoresChallengeInCache() throws Exception {
    var key = generateKey();
    stubAccount(key);

    var challenge = restClient.get()
        .uri("http://localhost:%s/public/auth/session/challenge?accountId=%s&keyId=%s"
            .formatted(port, ACCOUNT_ID, KEY_ID))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(AuthChallengeDto.class)
        .returnResult()
        .getResponseBody();

    assertThat(challenge.nonce()).isNotNull();
    assertThat(challengeCache.get(challenge.nonce())).isPresent();
  }

  @Test
  void testChallengeResponseHappyPath() throws Exception {
    var key = generateKey();
    stubAccount(key);

    var challenge = restClient.get()
        .uri("http://localhost:%s/public/auth/session/challenge?accountId=%s&keyId=%s"
            .formatted(port, ACCOUNT_ID, KEY_ID))
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(AuthChallengeDto.class)
        .returnResult()
        .getResponseBody();

    @SuppressWarnings("null")
    var signedJwt = createSignedJwt(key, challenge.nonce());
    var postBody = new AuthChallengeResponseDto(signedJwt);

    restClient.post()
        .uri("http://localhost:%s/public/auth/session/response".formatted(port))
        .bodyValue(postBody)
        .header("content-type", "application/json")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectHeader()
        .exists(SessionConfig.SESSION_HEADER);
  }

  @Test
  void testTooBigChallengeResponse() throws Exception {
    var postBody = new AuthChallengeResponseDto("a".repeat(20000));

    restClient.post()
        .uri("http://localhost:%s/public/auth/session/response".formatted(port))
        .bodyValue(postBody)
        .header("content-type", "application/json")
        .exchange()
        .expectStatus()
        .isEqualTo(401);
  }

  private void stubAccount(ECKey ecKey) {
    server.stubFor(get("/account/" + ACCOUNT_ID)
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
                ecKey.getKeyType().getValue(),
                ecKey.getKeyID(),
                ecKey.getAlgorithm().toString(),
                ecKey.getKeyUse(),
                ecKey.getCurve().toString(),
                ecKey.getX().toString(),
                ecKey.getY().toString()))));
  }

  private ECKey generateKey() throws Exception {
    return new ECKeyGenerator(Curve.P_256)
        .keyID(KEY_ID)
        .algorithm(Algorithm.NONE)
        .keyUse(KeyUse.SIGNATURE)
        .generate();
  }

  private String createSignedJwt(ECKey ecJwk, String nonce) throws JOSEException {
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
}
