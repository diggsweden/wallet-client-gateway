// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.clientconfig;

import java.util.Map;

public record ClientConfig(int cacheGeneration, Map<String, Boolean> features) {

  public ClientConfig {
    features = Map.copyOf(features);
  }
}
