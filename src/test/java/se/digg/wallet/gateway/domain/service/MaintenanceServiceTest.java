// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import se.digg.wallet.gateway.domain.model.maintenance.MaintenanceStatus;
import se.digg.wallet.gateway.support.FakeFeatureFlagPort;

class MaintenanceServiceTest {

  private static final Instant NOW = Instant.parse("2026-10-03T12:00:00Z");

  private final FakeFeatureFlagPort flags = new FakeFeatureFlagPort();
  private final MaintenanceService service =
      new MaintenanceService(flags, Clock.fixed(NOW, ZoneOffset.UTC));

  @Test
  void noMaintenanceWhenNoFlagsAreSet() {
    assertThat(service.status()).isEmpty();
    assertThat(service.active()).isEmpty();
  }

  @Test
  void announcesUpcomingWindowWithoutActivating() {
    window("2026-10-03T13:00:00Z", "2026-10-03T14:00:00Z");

    assertThat(service.status()).contains(new MaintenanceStatus(
        Instant.parse("2026-10-03T13:00:00Z"), Instant.parse("2026-10-03T14:00:00Z"), false));
    assertThat(service.active()).isEmpty();
  }

  @Test
  void activatesAtStart() {
    window("2026-10-03T12:00:00Z", "2026-10-03T14:00:00Z");

    assertThat(service.active()).hasValueSatisfying(status -> assertThat(status.active()).isTrue());
  }

  @Test
  void endsAtEnd() {
    window("2026-10-03T10:00:00Z", "2026-10-03T12:00:00Z");

    assertThat(service.status()).isEmpty();
  }

  @Test
  void acceptsOffsetTimestamps() {
    window("2026-10-03T13:00:00+02:00", "2026-10-03T15:00:00+02:00");

    assertThat(service.active()).hasValueSatisfying(status -> assertThat(status.endsAt())
        .isEqualTo(Instant.parse("2026-10-03T13:00:00Z")));
  }

  @Test
  void ignoresInvalidWindow() {
    window("not-a-time", "2026-10-03T14:00:00Z");

    assertThat(service.status()).isEmpty();
  }

  @Test
  void ignoresInvertedWindow() {
    window("2026-10-03T14:00:00Z", "2026-10-03T11:00:00Z");

    assertThat(service.status()).isEmpty();
  }

  @Test
  void ignoresWindowWithOnlyOneEnd() {
    flags.values.put(MaintenanceService.STARTS_AT, "2026-10-03T11:00:00Z");

    assertThat(service.status()).isEmpty();
  }

  @Test
  void toggleActivatesWithoutWindow() {
    flags.values.put(MaintenanceService.ENABLED, true);

    assertThat(service.active()).contains(new MaintenanceStatus(null, null, true));
  }

  @Test
  void toggleActivatesBeforePlannedWindowAndKeepsItsEnd() {
    flags.values.put(MaintenanceService.ENABLED, true);
    window("2026-10-03T13:00:00Z", "2026-10-03T14:00:00Z");

    assertThat(service.active()).contains(new MaintenanceStatus(
        Instant.parse("2026-10-03T13:00:00Z"), Instant.parse("2026-10-03T14:00:00Z"), true));
  }

  @Test
  void toggleIgnoresEndedWindow() {
    flags.values.put(MaintenanceService.ENABLED, true);
    window("2026-10-03T09:00:00Z", "2026-10-03T10:00:00Z");

    assertThat(service.active()).contains(new MaintenanceStatus(null, null, true));
  }

  @Test
  void returnsRetryAfterSecondsWhenActiveMaintenanceHasKnownEnd() {
    window("2026-10-03T11:00:00Z", "2026-10-03T12:10:00Z");

    assertThat(service.retryAfterSeconds(service.active().orElseThrow())).contains(601L);
  }

  @Test
  void omitsRetryAfterWhenEndIsUnknown() {
    flags.values.put(MaintenanceService.ENABLED, true);

    assertThat(service.retryAfterSeconds(service.active().orElseThrow())).isEmpty();
  }

  private void window(String startsAt, String endsAt) {
    flags.values.put(MaintenanceService.STARTS_AT, startsAt);
    flags.values.put(MaintenanceService.ENDS_AT, endsAt);
  }
}
