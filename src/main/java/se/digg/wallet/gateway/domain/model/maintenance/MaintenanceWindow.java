// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.maintenance;

import java.time.Instant;
import java.util.Objects;

/**
 * A planned maintenance window, from {@code startsAt} (inclusive) to {@code endsAt} (exclusive).
 */
public record MaintenanceWindow(Instant startsAt, Instant endsAt) {

  public MaintenanceWindow {
    Objects.requireNonNull(startsAt);
    Objects.requireNonNull(endsAt);
    if (!startsAt.isBefore(endsAt)) {
      throw new IllegalArgumentException("startsAt must be before endsAt");
    }
  }

  public boolean isActive(Instant now) {
    return !now.isBefore(startsAt) && now.isBefore(endsAt);
  }

  public boolean hasEnded(Instant now) {
    return !now.isBefore(endsAt);
  }
}
