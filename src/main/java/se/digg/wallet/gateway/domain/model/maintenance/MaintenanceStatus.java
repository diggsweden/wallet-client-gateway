// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.maintenance;

import jakarta.annotation.Nullable;
import java.time.Instant;

/**
 * Maintenance as announced to clients.
 *
 * @param startsAt when maintenance starts, or null when switched on without a planned window
 * @param endsAt when maintenance is expected to end, or null when unknown
 * @param active whether maintenance is in effect now, according to the server clock
 */
public record MaintenanceStatus(
    @Nullable Instant startsAt,
    @Nullable Instant endsAt,
    boolean active) {
}
