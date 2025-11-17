// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.auth.model;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class AuthChallengeCacheValueTest {

  @Test
  void testValidConstructorCall() {
    var timestamp = Instant.now();
    var randomString = UUID.randomUUID();
    var nonce = timestamp + "+" + randomString;
    var accountId = "ads";
    var ecKey = "abcdef";

    new AuthChallengeCacheValue(nonce, accountId, ecKey);
  }

  @Test
  void testGenerate() throws JOSEException {
    var accountId = "123";
    var ecKey = new ECKeyGenerator(Curve.P_256)
        .keyID("CORRET")
        .algorithm(Algorithm.NONE)
        .keyUse(KeyUse.SIGNATURE)
        .generate();

    var challenge = AuthChallengeCacheValue.generate(accountId, ecKey);
    assertThat(Stream.of(
        challenge.nonce(),
        challenge.accountId(),
        challenge.publicKey())).allMatch(Objects::nonNull);
  }

  @Test
  void testInvalidConstructorCall() {
    var timestamp = Instant.now();
    var randomString = UUID.randomUUID();
    var nonce = timestamp + " something other " + randomString;
    var accountId = "ads";

    assertThrows(NullPointerException.class,
        () -> new AuthChallengeCacheValue(nonce, accountId, null));
  }
}
