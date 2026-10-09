// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientContext;
import se.digg.wallet.gateway.domain.model.maintenance.MaintenanceStatus;
import se.digg.wallet.gateway.domain.model.maintenance.MaintenanceWindow;
import se.digg.wallet.gateway.domain.ports.outbound.FeatureFlagPort;

/**
 * Decides whether the service is in maintenance, from feature flags evaluated against the server
 * clock so clients never have to trust their own clocks.
 *
 * <p>
 * Maintenance is either planned, as a window from {@value #STARTS_AT} to {@value #ENDS_AT}
 * (ISO-8601 instants), or switched on directly with {@value #ENABLED}. When switched on, a window
 * that has not ended still supplies the expected end. A missing, unparsable or inverted window is
 * treated as no window: a broken flag file must never take the service down.
 */
public class MaintenanceService {

  static final String ENABLED = "gateway.maintenance.enabled";
  static final String STARTS_AT = "gateway.maintenance.starts-at";
  static final String ENDS_AT = "gateway.maintenance.ends-at";

  private final Logger logger = LoggerFactory.getLogger(MaintenanceService.class);

  private final FeatureFlagPort featureFlagPort;
  private final Clock clock;

  public MaintenanceService(FeatureFlagPort featureFlagPort, Clock clock) {
    this.featureFlagPort = featureFlagPort;
    this.clock = clock;
  }

  public Instant now() {
    return clock.instant();
  }

  /**
   * @return the current or upcoming maintenance, or empty when none is planned
   */
  public Optional<MaintenanceStatus> status() {
    var now = now();
    var window = window().filter(w -> !w.hasEnded(now));

    if (featureFlagPort.getBoolean(ENABLED, false, ClientContext.empty())) {
      return Optional.of(new MaintenanceStatus(
          window.map(MaintenanceWindow::startsAt).orElse(null),
          window.map(MaintenanceWindow::endsAt).orElse(null),
          true));
    }
    return window.map(w -> new MaintenanceStatus(w.startsAt(), w.endsAt(), w.isActive(now)));
  }

  /**
   * @return the maintenance in effect now, or empty when the service is available
   */
  public Optional<MaintenanceStatus> active() {
    return status().filter(MaintenanceStatus::active);
  }

  /**
   * @return Retry-After seconds for an active maintenance with a known end, or empty when clients
   *         should choose their own retry timing
   */
  public Optional<Long> retryAfterSeconds(MaintenanceStatus active) {
    if (active.endsAt() == null) {
      return Optional.empty();
    }
    var remaining = Duration.between(now(), active.endsAt()).toSeconds() + 1;
    return Optional.of(Math.max(1, remaining));
  }

  private Optional<MaintenanceWindow> window() {
    var startsAt = featureFlagPort.getString(STARTS_AT, "", ClientContext.empty());
    var endsAt = featureFlagPort.getString(ENDS_AT, "", ClientContext.empty());
    if (startsAt.isBlank() && endsAt.isBlank()) {
      return Optional.empty();
    }
    try {
      return Optional.of(new MaintenanceWindow(Instant.parse(startsAt), Instant.parse(endsAt)));
    } catch (DateTimeParseException | IllegalArgumentException e) {
      logger.warn("Ignoring invalid maintenance window {}={} {}={}: {}",
          STARTS_AT, startsAt, ENDS_AT, endsAt, e.getMessage());
      return Optional.empty();
    }
  }
}
