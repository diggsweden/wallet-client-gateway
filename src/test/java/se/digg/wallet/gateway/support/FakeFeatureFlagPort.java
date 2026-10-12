// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.support;

import java.util.HashMap;
import java.util.Map;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientContext;
import se.digg.wallet.gateway.domain.ports.outbound.FeatureFlagPort;

/** Returns values from a map, or the default when unset or of another type. */
public class FakeFeatureFlagPort implements FeatureFlagPort {

  public final Map<String, Object> values = new HashMap<>();

  @Override
  public boolean getBoolean(String key, boolean defaultValue, ClientContext context) {
    return values.get(key) instanceof Boolean value ? value : defaultValue;
  }

  @Override
  public int getInteger(String key, int defaultValue, ClientContext context) {
    return values.get(key) instanceof Integer value ? value : defaultValue;
  }

  @Override
  public String getString(String key, String defaultValue, ClientContext context) {
    return values.get(key) instanceof String value ? value : defaultValue;
  }
}
