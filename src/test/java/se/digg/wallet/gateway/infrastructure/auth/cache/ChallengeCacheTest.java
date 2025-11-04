// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.auth.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.infrastructure.auth.model.AuthChallengeCacheValue;

@ExtendWith(MockitoExtension.class)
class ChallengeCacheTest {

  @Mock
  private ApplicationConfig config = mock(ApplicationConfig.class);

  private ChallengeCache cache;

  @BeforeEach
  void beforeEach() {
    when(config.challengeCache()).thenReturn(
        new se.digg.wallet.gateway.application.config.ApplicationConfig.ChallengeCache(1));
    cache = new ChallengeCache(config);
  }


  @Test
  void storesValue() {
    var challenge = generateTestData();
    cache.store(challenge);
    assertThat(cache.get(challenge.nonce()))
        .isNotEmpty();
  }

  @Test
  void canOnlyUseNonceOnce() throws InterruptedException {
    var challenge = generateTestData();
    cache.store(challenge);
    assertThat(cache.get(challenge.nonce()))
        .isNotEmpty();
    assertThat(cache.get(challenge.nonce()))
        .isEmpty();
  }


  @Test
  void nonceIsRemovedAfterTtl()
      throws InterruptedException, IllegalArgumentException, IllegalAccessException {
    var challenge = generateTestData();
    cache.store(challenge);

    var fields = ReflectionUtils.findFields(ChallengeCache.class, f -> f.getName().equals("cache"),
        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN);

    var field = fields.get(0);
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    var map = (HashMap<String, AuthChallengeCacheValue>) field.get(cache);

    assertThat(map.get(challenge.nonce())).isNotNull();
    await()
        .atMost(5, TimeUnit.SECONDS)
        .until(() -> map.get(challenge.nonce()) == null);
  }

  private AuthChallengeCacheValue generateTestData() {
    try {
      var ecKey = new ECKeyGenerator(Curve.P_256)
          .algorithm(Algorithm.NONE)
          .keyUse(KeyUse.SIGNATURE)
          .generate();
      return AuthChallengeCacheValue.generate("123", ecKey);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
