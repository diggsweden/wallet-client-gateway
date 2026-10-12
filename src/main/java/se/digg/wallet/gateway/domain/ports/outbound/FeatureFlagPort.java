// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.ports.outbound;

import se.digg.wallet.gateway.domain.model.clientconfig.ClientContext;

/**
 * Evaluates feature flags for a client. Implementations must never throw: when a flag is missing or
 * cannot be evaluated, the given default is returned.
 */
public interface FeatureFlagPort {

  boolean getBoolean(String key, boolean defaultValue, ClientContext context);

  int getInteger(String key, int defaultValue, ClientContext context);

  String getString(String key, String defaultValue, ClientContext context);
}
