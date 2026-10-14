// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.ports.outbound;

import java.util.List;
import se.digg.wallet.gateway.domain.model.lote.LoteProvider;

public interface LoteProviderPort {

  List<LoteProvider> getLoteProviders();
}
