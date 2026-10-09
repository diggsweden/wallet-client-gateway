// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.filter;

import static se.digg.wallet.gateway.application.controller.ProblemType.MAINTENANCE;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.domain.exception.UnavailableDueToMaintenanceException;
import se.digg.wallet.gateway.domain.service.MaintenanceService;

/**
 * Rejects requests with 503 and Retry-After while a maintenance window is active. Runs before
 * Spring Security so no downstream calls are made, but after the logging filter so rejections are
 * logged.
 */
@Component
public class MaintenanceFilter extends OncePerRequestFilter {

  private final MaintenanceService maintenanceService;
  private final HandlerExceptionResolver exceptionResolver;
  private final List<String> allowedPaths;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  @Autowired
  public MaintenanceFilter(
      MaintenanceService maintenanceService,
      @Qualifier("handlerExceptionResolver")
      HandlerExceptionResolver exceptionResolver,
      ApplicationConfig applicationConfig) {
    this(maintenanceService, exceptionResolver, applicationConfig.maintenance());
  }

  MaintenanceFilter(
      MaintenanceService maintenanceService,
      HandlerExceptionResolver exceptionResolver,
      ApplicationConfig.Maintenance maintenance) {
    this.maintenanceService = maintenanceService;
    this.exceptionResolver = exceptionResolver;
    this.allowedPaths = maintenance.allowedPaths();
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    var path = request.getServletPath();
    return allowedPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    var active = maintenanceService.active();
    if (active.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    response.setStatus(MAINTENANCE.getHttpStatus().value());
    maintenanceService.retryAfterSeconds(active.get())
        .ifPresent(seconds -> response.setHeader(HttpHeaders.RETRY_AFTER, seconds.toString()));
    response.setHeader(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue());
    var exception = new UnavailableDueToMaintenanceException(request.getServletPath());
    if (exceptionResolver.resolveException(request, response, null, exception) == null) {
      throw exception;
    }
  }
}
