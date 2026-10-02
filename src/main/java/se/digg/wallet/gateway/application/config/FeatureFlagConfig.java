// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import dev.openfeature.contrib.providers.flagd.Config;
import dev.openfeature.contrib.providers.flagd.FlagdOptions;
import dev.openfeature.contrib.providers.flagd.FlagdProvider;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import se.digg.wallet.gateway.api.v0.ClientConfigApi;
import se.digg.wallet.gateway.domain.ports.outbound.FeatureFlagPort;
import se.digg.wallet.gateway.domain.service.ClientConfigService;
import se.digg.wallet.gateway.infrastructure.featureflag.OpenFeatureAdapter;

/**
 * Feature flags are read in-process by the flagd provider from a flagd JSON flag file, which is
 * polled for changes. No flagd sidecar or daemon is needed.
 */
@Configuration
public class FeatureFlagConfig {

  static final String DOMAIN = "wallet-client-gateway";

  private final Logger logger = LoggerFactory.getLogger(FeatureFlagConfig.class);

  @Bean(destroyMethod = "shutdown")
  public OpenFeatureAPI openFeatureApi(ApplicationConfig applicationConfig) {
    var filePath = applicationConfig.featureFlags().filePath();
    var provider = new FlagdProvider(FlagdOptions.builder()
        .resolverType(Config.Resolver.FILE)
        .offlineFlagSourcePath(filePath)
        .build());

    var api = OpenFeatureAPI.getInstance();
    try {
      api.setProviderAndWait(DOMAIN, provider);
    } catch (OpenFeatureError e) {
      // Keep starting: every flag falls back to its safe default until the file becomes readable.
      logger.warn("Feature flag provider not ready, flags use defaults. File: {}", filePath, e);
    }
    return api;
  }

  @Bean
  public FeatureFlagPort featureFlagPort(OpenFeatureAPI openFeatureApi) {
    return new OpenFeatureAdapter(openFeatureApi.getClient(DOMAIN));
  }

  @Bean
  public ClientConfigService clientConfigService(
      FeatureFlagPort featureFlagPort, ApplicationConfig applicationConfig) {
    return new ClientConfigService(
        featureFlagPort, applicationConfig.clientConfig().features());
  }

  /**
   * Lets clients revalidate the client config cheaply: a request with a matching If-None-Match gets
   * an empty 304. Runs after Spring Security, so unauthorized requests are still rejected.
   */
  @Bean
  public FilterRegistrationBean<ShallowEtagHeaderFilter> clientConfigEtagFilter() {
    var registration = new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
    registration.addUrlPatterns(ClientConfigApi.PATH_GET_CLIENT_CONFIG);
    return registration;
  }
}
