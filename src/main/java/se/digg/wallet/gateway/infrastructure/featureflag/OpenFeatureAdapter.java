// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.featureflag;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.MutableContext;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientContext;
import se.digg.wallet.gateway.domain.ports.outbound.FeatureFlagPort;

/**
 * Evaluates flags through OpenFeature. The OpenFeature client already returns the default value on
 * any evaluation error (missing flag, type mismatch, provider not ready), which satisfies the
 * never-throw contract of {@link FeatureFlagPort}.
 */
public class OpenFeatureAdapter implements FeatureFlagPort {

  static final String OS = "os";
  static final String OS_VERSION = "osVersion";
  static final String DEVICE_MODEL = "deviceModel";
  static final String APP_VERSION = "appVersion";

  private final Client client;

  public OpenFeatureAdapter(Client client) {
    this.client = client;
  }

  @Override
  public boolean getBoolean(String key, boolean defaultValue, ClientContext context) {
    return client.getBooleanValue(key, defaultValue, toEvaluationContext(context));
  }

  @Override
  public int getInteger(String key, int defaultValue, ClientContext context) {
    return client.getIntegerValue(key, defaultValue, toEvaluationContext(context));
  }

  static EvaluationContext toEvaluationContext(ClientContext context) {
    var result = new MutableContext();
    putIfPresent(result, OS, context.os());
    putIfPresent(result, OS_VERSION, context.osVersion());
    putIfPresent(result, DEVICE_MODEL, context.deviceModel());
    putIfPresent(result, APP_VERSION, context.appVersion());
    return result;
  }

  private static void putIfPresent(MutableContext context, String key, String value) {
    if (value != null && !value.isBlank()) {
      context.add(key, value);
    }
  }
}
