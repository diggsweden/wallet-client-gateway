// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientConfig;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientContext;
import se.digg.wallet.gateway.domain.ports.outbound.FeatureFlagPort;

/**
 * Builds the remote configuration handed to wallet clients from feature flags. Every value has a
 * safe default so that a missing flag or an unavailable flag source never triggers a cache reset or
 * enables a feature.
 */
public class ClientConfigService {

  static final String CACHE_GENERATION = "client.cache-generation";
  static final String FEATURE_PREFIX = "client.feature.";

  private final Logger logger = LoggerFactory.getLogger(ClientConfigService.class);

  private final FeatureFlagPort featureFlagPort;
  private final List<String> features;

  /**
   * @param features the feature names exposed to clients; only these are evaluated and returned
   */
  public ClientConfigService(FeatureFlagPort featureFlagPort, List<String> features) {
    this.featureFlagPort = featureFlagPort;
    this.features = List.copyOf(features);
  }

  public ClientConfig getClientConfig(ClientContext context) {
    return new ClientConfig(cacheGeneration(context), features(context));
  }

  /**
   * Clients reset their local cache when this is greater than the last value they saw, so it must
   * only ever be increased. The default 0 never triggers a reset.
   */
  private int cacheGeneration(ClientContext context) {
    var value = featureFlagPort.getInteger(CACHE_GENERATION, 0, context);
    if (value < 0) {
      logger.warn("Ignoring negative flag {}: {}", CACHE_GENERATION, value);
      return 0;
    }
    return value;
  }

  private Map<String, Boolean> features(ClientContext context) {
    var result = new LinkedHashMap<String, Boolean>();
    features.forEach(
        name -> result.put(name,
            featureFlagPort.getBoolean(FEATURE_PREFIX + name, false, context)));
    return result;
  }
}
