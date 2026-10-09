// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.clientconfig;

import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.api.v0.model.ClientConfigResponse;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientConfig;

@Component
public class ClientConfigMapper {

  public ClientConfigResponse toResponse(ClientConfig clientConfig) {
    return ClientConfigResponse.builder()
        .cacheGeneration(clientConfig.cacheGeneration())
        .features(clientConfig.features())
        .build();
  }
}
