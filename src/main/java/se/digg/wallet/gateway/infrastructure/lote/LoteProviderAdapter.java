// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.lote;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.domain.model.lote.LoteProvider;
import se.digg.wallet.gateway.domain.model.lote.LoteProviderBuilder;
import se.digg.wallet.gateway.domain.ports.outbound.LoteProviderPort;

@Component
public class LoteProviderAdapter implements LoteProviderPort {

  @Autowired
  private ApplicationConfig applicationConfig;

  @Override
  public List<LoteProvider> getLoteProviders() {
    return Stream.ofNullable(applicationConfig.loteProviders())
        .flatMap(Collection::stream)
        .filter(Objects::nonNull)
        .map(item -> LoteProviderBuilder.builder().id(item.id()).url(item.url()).build())
        .toList();
  }
}
