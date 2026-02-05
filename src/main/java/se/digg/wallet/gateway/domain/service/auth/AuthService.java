// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.auth;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeDto;
import se.digg.wallet.gateway.application.model.auth.ValidateAuthChallengeRequestDto;
import se.digg.wallet.gateway.infrastructure.account.client.WalletAccountClient;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountAccountDto;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountJwkDto;
import se.digg.wallet.gateway.infrastructure.auth.cache.ChallengeCache;
import se.digg.wallet.gateway.infrastructure.auth.model.AuthChallengeCacheValue;

@Service
public class AuthService {
  private final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final ChallengeCache challengeCache;
  private final WalletAccountClient walletAccountClient;

  public AuthService(ChallengeCache challengeCache,
      WalletAccountClient walletAccountClient) {
    this.challengeCache = challengeCache;
    this.walletAccountClient = walletAccountClient;
  }

  public AuthChallengeDto initChallenge(String accountId, String keyId) {
    var ecKey = getPublicEcKey(accountId, keyId);

    if (ecKey.isEmpty()) {
      // generate nonce but do not cache it
      // rate limit this case harshly
      return new AuthChallengeDto(AuthChallengeCacheValue.generateNonce());
    }

    var challenge = AuthChallengeCacheValue.generate(accountId, ecKey.get());
    challengeCache.store(challenge);

    return new AuthChallengeDto(challenge.nonce());
  }

  public Optional<ValidationResult> validateChallenge(
      ValidateAuthChallengeRequestDto validateAuthChallengeRequestDto) {
    try {
      SignedJWT signedJwt = SignedJWT.parse(validateAuthChallengeRequestDto.signedJwt());
      String nonce = signedJwt.getJWTClaimsSet().getClaimAsString("nonce");

      var challengeValue = challengeCache.get(nonce);
      if (challengeValue.isEmpty()) {
        log.info("Invalid nonce {}", nonce);
        return Optional.empty();
      }

      var accountId = challengeValue.get().accountId();
      var ecPublicJwk = JWK.parse(challengeValue.get().publicKey()).toECKey();

      if (!signedJwt.getHeader().getKeyID().equals(ecPublicJwk.getKeyID())) {
        log.info("KeyID not match");
        return Optional.empty();
      }
      JWSVerifier verifier = new ECDSAVerifier(ecPublicJwk);

      if (signedJwt.verify(verifier)) {
        log.debug("Account {} challenge verified", accountId);
        return Optional.of(new ValidationResult(accountId));
      } else {
        log.info("Auth challenge not verified for account {}", accountId);
        return Optional.empty();
      }
    } catch (ParseException parseException) {
      log.info("Unable to parse claimset of signed JWT, deny authchallange. Cause {}",
          parseException.getMessage());
      return Optional.empty();
    } catch (JOSEException joseException) {
      log.info("Unable to verify auth challenge {}", joseException.getMessage());
      return Optional.empty();
    }
  }

  private Optional<ECKey> getPublicEcKey(String accountId, String keyId) {
    if (accountId == null || accountId.isEmpty()) {
      log.info("Account ID empty, cannot handle auth challange");
      return Optional.empty();
    }
    Optional<WalletAccountAccountDto> account = walletAccountClient.getAccount(accountId);
    if (account.isEmpty()) {
      log.info("No account present with id {}", accountId);
      return Optional.empty();
    }
    try {
      WalletAccountJwkDto publicKey = account.get().publicKey();
      if (!keyId.equals(publicKey.kid())) {
        log.info("No kid {} present on accountId {}", keyId, accountId);
        return Optional.empty();
      }
      ECKey publicEcKey = new ECKey.Builder(
          Curve.parse(publicKey.crv()),
          Base64URL.from(publicKey.x()),
          Base64URL.from(publicKey.y()))
          .algorithm(Algorithm.parse(publicKey.alg()))
          .keyID(publicKey.kid())
          .keyUse(KeyUse.parse(publicKey.use()))
          .build();
      return Optional.of(publicEcKey.toPublicJWK());

    } catch (ParseException parseException) {
      log.info("Unable to parse claimset of signed JWT, deny authchallange. Cause {}",
          parseException.getMessage());
      return Optional.empty();
    }
  }

  public record ValidationResult(String accountId) {
  }

}
