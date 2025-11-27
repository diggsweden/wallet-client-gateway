// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.digg.wallet.gateway.application.model.WalletAccountAccountDtoTestBuilder;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeResponseDto;
import se.digg.wallet.gateway.infrastructure.account.client.WalletAccountClient;
import se.digg.wallet.gateway.infrastructure.auth.cache.ChallengeCache;
import se.digg.wallet.gateway.infrastructure.auth.model.AuthChallengeCacheValue;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  ChallengeCache challengeCache;

  @Mock
  WalletAccountClient walletAccountClient;

  @InjectMocks
  AuthService authService;
  static String accountId = UUID.randomUUID().toString();

  @Test
  void validInputResultsInCachedValue() throws Exception {
    var keyId = "kid";
    var ecJwk = new ECKeyGenerator(Curve.P_256)
        .keyID(keyId)
        .algorithm(Algorithm.NONE)
        .keyUse(KeyUse.SIGNATURE)
        .generate();
    var ecPublicJwk = ecJwk.toPublicJWK();

    var walletAccountAccountDto =
        WalletAccountAccountDtoTestBuilder.generateWalletAccount(ecPublicJwk)
            .personalIdentityNumber(accountId).build();

    when(walletAccountClient.getAccount(anyString()))
        .thenReturn(Optional.of(walletAccountAccountDto));

    var challenge = authService.initChallenge(accountId, keyId);

    assertThat(challenge).isNotNull();
    verify(challengeCache).store(any());
  }

  @Test
  void accountNotFoundResultsInNothingCached() {
    var keyId = "kid";

    when(walletAccountClient.getAccount(anyString()))
        .thenReturn(Optional.empty());

    var challenge = authService.initChallenge(accountId, keyId);

    assertThat(challenge).isNotNull();
    verify(challengeCache, never()).store(any());
  }

  @Test
  void kidNotFoundResultsInNothingCached() throws Exception {
    var keyId = "kid";
    var ecJwk = new ECKeyGenerator(Curve.P_256)
        .keyID(keyId)
        .algorithm(Algorithm.NONE)
        .keyUse(KeyUse.SIGNATURE)
        .generate();
    var ecPublicJwk = ecJwk.toPublicJWK();

    var walletAccountAccountDto =
        WalletAccountAccountDtoTestBuilder.generateWalletAccount(ecPublicJwk)
            .personalIdentityNumber(accountId).build();

    when(walletAccountClient.getAccount(anyString()))
        .thenReturn(Optional.of(walletAccountAccountDto));

    var challenge = authService.initChallenge(accountId, "WRONG_KID");

    assertThat(challenge).isNotNull();
    verify(challengeCache, never()).store(any());
  }


  @Test
  void testValidateChallenge() throws JOSEException {
    ECKey ecJwk = new ECKeyGenerator(Curve.P_256)
        .keyID("123")
        .algorithm(Algorithm.NONE)
        .keyUse(KeyUse.SIGNATURE)
        .generate();
    String nonce = AuthChallengeCacheValue.generate(accountId, ecJwk).nonce();

    when(challengeCache.get(nonce))
        .thenReturn(Optional.of(fromNonce(nonce, ecJwk)));

    String signedJwt = createSignedJwt(ecJwk, nonce);
    AuthChallengeResponseDto authChallangeResonse = new AuthChallengeResponseDto(signedJwt);
    assertThat(authService.validateChallenge(authChallangeResonse)).isPresent();
  }


  @Test
  void testInvalidNonce() throws JOSEException {
    ECKey ecJwk = new ECKeyGenerator(Curve.P_256)
        .keyID("123")
        .algorithm(Algorithm.NONE)
        .keyUse(KeyUse.SIGNATURE)
        .generate();
    String nonce = AuthChallengeCacheValue.generate(accountId, ecJwk).nonce();
    when(challengeCache.get(nonce)).thenReturn(Optional.empty());
    String signedJwt = createSignedJwt(ecJwk, nonce);
    AuthChallengeResponseDto authChallangeResonse = new AuthChallengeResponseDto(signedJwt);
    assertThat(authService.validateChallenge(authChallangeResonse)).isNotPresent();
  }

  @Test
  void testWrongKid() throws Exception {
    ECKey ecJwk = new ECKeyGenerator(Curve.P_256)
        .keyID("CORRET")
        .algorithm(Algorithm.NONE)
        .keyUse(KeyUse.SIGNATURE)
        .generate();
    String nonce = AuthChallengeCacheValue.generate(accountId, ecJwk).nonce();

    var ecPublicJwkMap = ecJwk.toPublicJWK().toJSONObject();
    ecPublicJwkMap.put("kid", "WRONG");
    var ecPublicJwk = ECKey.parse(ecPublicJwkMap);

    when(challengeCache.get(nonce))
        .thenReturn(Optional.of(fromNonce(nonce, ecPublicJwk)));

    String signedJwt = createSignedJwt(ecJwk, nonce);
    AuthChallengeResponseDto authChallangeResonse = new AuthChallengeResponseDto(signedJwt);
    assertThat(authService.validateChallenge(authChallangeResonse)).isNotPresent();
  }

  @Test
  void testBadSignature() throws JOSEException {
    ECKey ecJwk = new ECKeyGenerator(Curve.P_256)
        .keyID("123")
        .algorithm(Algorithm.NONE)
        .keyUse(KeyUse.SIGNATURE)
        .generate();
    String nonce = AuthChallengeCacheValue.generate(accountId, ecJwk).nonce();

    when(challengeCache.get(nonce))
        .thenReturn(Optional.of(fromNonce(nonce, ecJwk)));

    String signedJwt = createSignedJwt(ecJwk, nonce) + "bad";
    AuthChallengeResponseDto authChallangeResonse = new AuthChallengeResponseDto(signedJwt);
    assertThat(authService.validateChallenge(authChallangeResonse)).isEmpty();
  }

  private String createSignedJwt(ECKey ecJwk, String nonce) throws JOSEException {
    JWSSigner signer = new ECDSASigner(ecJwk);
    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .claim("accountId", accountId)
        .claim("nonce", nonce)
        .expirationTime(new Date(new Date().getTime() + 60 * 1000))
        .build();
    SignedJWT signedJwt = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(ecJwk.getKeyID()).build(),
        claimsSet);

    signedJwt.sign(signer);

    return signedJwt.serialize();
  }

  static AuthChallengeCacheValue fromNonce(String nonce, ECKey ecJwk) {
    return new AuthChallengeCacheValue(nonce, accountId, ecJwk.toJSONString());
  }
}
