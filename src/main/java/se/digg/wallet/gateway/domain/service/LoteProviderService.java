// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.domain.model.lote.LoteProvider;
import se.digg.wallet.gateway.domain.ports.outbound.LoteProviderPort;

@Service
public class LoteProviderService {

  @Autowired
  private LoteProviderPort loteProviderPort;

  public List<LoteProvider> getLoteProviders() {
    return loteProviderPort.getLoteProviders();
  }
}
