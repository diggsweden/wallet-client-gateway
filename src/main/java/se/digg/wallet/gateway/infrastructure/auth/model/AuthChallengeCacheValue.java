// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.auth.model;

import com.nimbusds.jose.jwk.ECKey;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AuthChallengeCacheValue(
    String nonce,
    Instant timestamp,
    UUID randomString,
    String accountId,
    String publicKey) {

  public AuthChallengeCacheValue {
    Objects.requireNonNull(nonce);
    Objects.requireNonNull(timestamp);
    Objects.requireNonNull(randomString);
    Objects.requireNonNull(accountId);
    Objects.requireNonNull(publicKey);

    if (!nonce.equals(timestamp + "+" + randomString)) {
      throw new IllegalArgumentException(
          """
                  Nonce does not match timestamp and randomString.
                  Nonce: %s
                  Timestamp: %s
                  RandomString: %s
              """
              .formatted(nonce, timestamp, randomString));
    }
  }

  public static AuthChallengeCacheValue generate(String accountId, ECKey ecKey) {
    var timestamp = Instant.now();
    var randomString = UUID.randomUUID();
    var nonce = generateNonce(timestamp, randomString);
    var publicKey = ecKey.toPublicJWK().toJSONString();

    return new AuthChallengeCacheValue(nonce, timestamp, randomString, accountId, publicKey);
  }

  public static String generateNonce() {
    var timestamp = Instant.now();
    var randomString = UUID.randomUUID();
    return generateNonce(timestamp, randomString);
  }

  private static String generateNonce(Instant timestamp, UUID randomString) {
    return timestamp + "+" + randomString;
  }

}
