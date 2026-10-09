// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientContext;
import se.digg.wallet.gateway.domain.ports.outbound.FeatureFlagPort;

class ClientConfigServiceTest {

  private final FakeFeatureFlagPort flags = new FakeFeatureFlagPort();

  @Test
  void usesSafeDefaultsWhenNoFlagsAreSet() {
    var service = new ClientConfigService(flags, List.of("example"));

    var config = service.getClientConfig(ClientContext.empty());

    assertThat(config.cacheGeneration()).isZero();
    assertThat(config.features()).containsExactly(Map.entry("example", false));
  }

  @Test
  void returnsFlagValues() {
    flags.values.put(ClientConfigService.CACHE_GENERATION, 3);
    flags.values.put(ClientConfigService.FEATURE_PREFIX + "example", true);
    var service = new ClientConfigService(flags, List.of("example"));

    var config = service.getClientConfig(ClientContext.empty());

    assertThat(config.cacheGeneration()).isEqualTo(3);
    assertThat(config.features()).containsExactly(Map.entry("example", true));
  }

  @Test
  void onlyReturnsConfiguredFeatures() {
    flags.values.put(ClientConfigService.FEATURE_PREFIX + "secret", true);
    var service = new ClientConfigService(flags, List.of("example"));

    var config = service.getClientConfig(ClientContext.empty());

    assertThat(config.features()).containsOnlyKeys("example");
  }

  @Test
  void ignoresNegativeCacheGeneration() {
    flags.values.put(ClientConfigService.CACHE_GENERATION, -1);
    var service = new ClientConfigService(flags, List.of());

    var config = service.getClientConfig(ClientContext.empty());

    assertThat(config.cacheGeneration()).isZero();
  }

  private static class FakeFeatureFlagPort implements FeatureFlagPort {

    final Map<String, Object> values = new HashMap<>();

    @Override
    public boolean getBoolean(String key, boolean defaultValue, ClientContext context) {
      return values.get(key) instanceof Boolean value ? value : defaultValue;
    }

    @Override
    public int getInteger(String key, int defaultValue, ClientContext context) {
      return values.get(key) instanceof Integer value ? value : defaultValue;
    }
  }
}
