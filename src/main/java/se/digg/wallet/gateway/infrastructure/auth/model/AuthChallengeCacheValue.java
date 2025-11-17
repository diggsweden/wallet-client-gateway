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
    String accountId,
    String publicKey) {

  public AuthChallengeCacheValue {
    Objects.requireNonNull(nonce);
    Objects.requireNonNull(accountId);
    Objects.requireNonNull(publicKey);
  }

  public static AuthChallengeCacheValue generate(String accountId, ECKey ecKey) {
    var nonce = generateNonce(Instant.now(), UUID.randomUUID());
    var publicKey = ecKey.toPublicJWK().toJSONString();

    return new AuthChallengeCacheValue(nonce, accountId, publicKey);
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
