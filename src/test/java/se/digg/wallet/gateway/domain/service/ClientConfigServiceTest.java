// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientContext;
import se.digg.wallet.gateway.domain.model.maintenance.MaintenanceStatus;
import se.digg.wallet.gateway.support.FakeFeatureFlagPort;

class ClientConfigServiceTest {

  private static final Instant NOW = Instant.parse("2026-10-03T12:00:00Z");

  private final FakeFeatureFlagPort flags = new FakeFeatureFlagPort();
  private final MaintenanceService maintenanceService =
      new MaintenanceService(flags, Clock.fixed(NOW, ZoneOffset.UTC));

  @Test
  void usesSafeDefaultsWhenNoFlagsAreSet() {
    var service = new ClientConfigService(flags, maintenanceService, List.of("example"));

    var config = service.getClientConfig(ClientContext.empty());

    assertThat(config.cacheGeneration()).isZero();
    assertThat(config.features()).containsExactly(Map.entry("example", false));
    assertThat(config.maintenance()).isNull();
  }

  @Test
  void returnsFlagValues() {
    flags.values.put(ClientConfigService.CACHE_GENERATION, 3);
    flags.values.put(ClientConfigService.FEATURE_PREFIX + "example", true);
    var service = new ClientConfigService(flags, maintenanceService, List.of("example"));

    var config = service.getClientConfig(ClientContext.empty());

    assertThat(config.cacheGeneration()).isEqualTo(3);
    assertThat(config.features()).containsExactly(Map.entry("example", true));
  }

  @Test
  void onlyReturnsConfiguredFeatures() {
    flags.values.put(ClientConfigService.FEATURE_PREFIX + "secret", true);
    var service = new ClientConfigService(flags, maintenanceService, List.of("example"));

    var config = service.getClientConfig(ClientContext.empty());

    assertThat(config.features()).containsOnlyKeys("example");
  }

  @Test
  void ignoresNegativeCacheGeneration() {
    flags.values.put(ClientConfigService.CACHE_GENERATION, -1);
    var service = new ClientConfigService(flags, maintenanceService, List.of());

    var config = service.getClientConfig(ClientContext.empty());

    assertThat(config.cacheGeneration()).isZero();
  }

  @Test
  void announcesUpcomingMaintenance() {
    flags.values.put(MaintenanceService.STARTS_AT, "2026-10-03T20:00:00Z");
    flags.values.put(MaintenanceService.ENDS_AT, "2026-10-03T22:00:00Z");
    var service = new ClientConfigService(flags, maintenanceService, List.of());

    var config = service.getClientConfig(ClientContext.empty());

    assertThat(config.maintenance()).isEqualTo(new MaintenanceStatus(
        Instant.parse("2026-10-03T20:00:00Z"), Instant.parse("2026-10-03T22:00:00Z"), false));
  }
}
