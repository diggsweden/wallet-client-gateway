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

  @Override
  public String getString(String key, String defaultValue, ClientContext context) {
    return client.getStringValue(key, defaultValue, toEvaluationContext(context));
  }

  static EvaluationContext toEvaluationContext(ClientContext context) {
    var result = new MutableContext();
    putIfPresent(result, ClientContext.OS_ATTRIBUTE, context.os());
    putIfPresent(result, ClientContext.OS_VERSION_ATTRIBUTE, context.osVersion());
    putIfPresent(result, ClientContext.DEVICE_MODEL_ATTRIBUTE, context.deviceModel());
    putIfPresent(result, ClientContext.APP_VERSION_ATTRIBUTE, context.appVersion());
    return result;
  }

  private static void putIfPresent(MutableContext context, String key, String value) {
    if (value != null && !value.isBlank()) {
      context.add(key, value);
    }
  }
}
