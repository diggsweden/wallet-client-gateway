// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.clientconfig;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.api.v0.model.ClientConfigResponse;
import se.digg.wallet.gateway.api.v0.model.MaintenanceWindow;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientConfig;
import se.digg.wallet.gateway.domain.model.maintenance.MaintenanceStatus;

@Component
public class ClientConfigMapper {

  public ClientConfigResponse toResponse(ClientConfig clientConfig) {
    return ClientConfigResponse.builder()
        .cacheGeneration(clientConfig.cacheGeneration())
        .features(clientConfig.features())
        .maintenance(toMaintenanceWindow(clientConfig.maintenance()))
        .build();
  }

  public MaintenanceWindow toMaintenanceWindow(MaintenanceStatus maintenance) {
    if (maintenance == null) {
      return null;
    }
    return MaintenanceWindow.builder()
        .startsAt(toOffsetDateTime(maintenance.startsAt()))
        .endsAt(toOffsetDateTime(maintenance.endsAt()))
        .active(maintenance.active())
        .build();
  }

  private static OffsetDateTime toOffsetDateTime(Instant instant) {
    return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
  }
}
