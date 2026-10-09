// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.clientconfig;

import jakarta.annotation.Nullable;
import java.util.Map;
import se.digg.wallet.gateway.domain.model.maintenance.MaintenanceStatus;

/**
 * @param maintenance current or upcoming maintenance, or null when none is planned
 */
public record ClientConfig(
    int cacheGeneration,
    Map<String, Boolean> features,
    @Nullable MaintenanceStatus maintenance) {

  public ClientConfig {
    features = Map.copyOf(features);
  }
}
