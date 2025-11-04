// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.exception.ApiKeyNeededException;

@Component
public class ApiKeyVerifier {

  private final String key;

  public ApiKeyVerifier(ApplicationConfig config) {
    this.key = Objects.requireNonNull(config.apisecret());
  }

  public void verify() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    var matchingApiKey = Optional.ofNullable(attrs)
        .map(ServletRequestAttributes::getRequest)
        .map(request -> request.getHeader(SecurityConfig.API_KEY_HEADER))
        .filter(header -> key.equals(header))
        .isPresent();
    if (!matchingApiKey) {
      throw new ApiKeyNeededException();
    }
  }
}
