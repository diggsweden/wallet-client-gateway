// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import se.digg.wallet.gateway.application.mapper.clientconfig.ClientContextMapper;

import java.io.IOException;
import java.util.List;

public class ClientContextMdcFilter extends OncePerRequestFilter {

  public static final String MDC_DEVICE_OS_NAME = "client.os.name";
  public static final String MDC_DEVICE_OS_VERSION = "client.os.version";
  public static final String MDC_DEVICE_MODEL_NAME = "client.device.model.name";
  public static final String MDC_APP_VERSION = "client.app.version";
  public static final String MDC_APP_BUILD = "client.app.build";
  public static final List<String> TRACKED_KEYS = List.of(
      MDC_DEVICE_OS_NAME,
      MDC_DEVICE_OS_VERSION,
      MDC_DEVICE_MODEL_NAME,
      MDC_APP_VERSION,
      MDC_APP_BUILD);

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    try {
      var context = ClientContextMapper.from(request);
      putIfPresent(MDC_DEVICE_OS_NAME, sanitize(context.os()));
      putIfPresent(MDC_DEVICE_OS_VERSION, sanitize(context.osVersion()));
      putIfPresent(MDC_DEVICE_MODEL_NAME, sanitize(context.deviceModel()));
      putIfPresent(MDC_APP_VERSION, sanitize(context.appVersion()));
      putIfPresent(MDC_APP_BUILD, sanitize(context.appBuild()));

      filterChain.doFilter(request, response);

    } finally {
      TRACKED_KEYS.forEach(MDC::remove);
    }
  }

  /**
   * Prevent log injection via malicious newline characters.
   */
  private String sanitize(String input) {
    if (input == null) {
      return null;
    }
    return input.replaceAll("[\\r\\n]", "");
  }

  /**
   * Add a diagnostic context value to the MDC if present.
   */
  private void putIfPresent(String key, String value) {
    if (StringUtils.hasText(value)) {
      MDC.put(key, value);
    }
  }
}
