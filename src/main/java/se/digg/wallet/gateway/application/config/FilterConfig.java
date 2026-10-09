// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import se.digg.wallet.gateway.api.v0.ClientUtilsApi;
import se.digg.wallet.gateway.application.filter.LoggingFilter;
import se.digg.wallet.gateway.application.filter.MaintenanceFilter;

@Configuration
public class FilterConfig {

  private static final int LOGGING_ORDER = Ordered.HIGHEST_PRECEDENCE;
  private static final int MAINTENANCE_ORDER = LOGGING_ORDER + 10;
  private static final int CLIENT_CONFIG_ETAG_ORDER = Ordered.LOWEST_PRECEDENCE;

  @Bean
  public FilterRegistrationBean<LoggingFilter> loggingFilterRegistration(LoggingFilter filter) {
    var registration = new FilterRegistrationBean<>(filter);
    registration.setOrder(LOGGING_ORDER);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<MaintenanceFilter> maintenanceFilterRegistration(
      MaintenanceFilter filter) {
    var registration = new FilterRegistrationBean<>(filter);
    registration.setOrder(MAINTENANCE_ORDER);
    return registration;
  }

  /**
   * Lets clients revalidate the client config cheaply: a request with a matching If-None-Match gets
   * an empty 304. Runs after Spring Security, so unauthorized requests are still rejected.
   */
  @Bean
  public FilterRegistrationBean<ShallowEtagHeaderFilter> clientConfigEtagFilter() {
    var registration = new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
    registration.addUrlPatterns(ClientUtilsApi.PATH_GET_CLIENT_CONFIG);
    registration.setOrder(CLIENT_CONFIG_ETAG_ORDER);
    return registration;
  }
}
