// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.ClientConfigApi;
import se.digg.wallet.gateway.api.v0.model.ClientConfigResponse;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.mapper.clientconfig.ClientConfigMapper;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientContext;
import se.digg.wallet.gateway.domain.service.ClientConfigService;

@RestController
public class ClientConfigController implements ClientConfigApi {

  static final String DEVICE_OS = "Wallet-Device-OS";
  static final String DEVICE_OS_VERSION = "Wallet-Device-OS-Version";
  static final String DEVICE_MODEL = "Wallet-Device-Model";
  static final String APP_VERSION = "Wallet-App-Version";

  private final ClientConfigService clientConfigService;
  private final ClientConfigMapper clientConfigMapper;
  private final ApplicationConfig applicationConfig;
  private final HttpServletRequest request;

  public ClientConfigController(
      ClientConfigService clientConfigService,
      ClientConfigMapper clientConfigMapper,
      ApplicationConfig applicationConfig,
      HttpServletRequest request) {
    this.clientConfigService = clientConfigService;
    this.clientConfigMapper = clientConfigMapper;
    this.applicationConfig = applicationConfig;
    this.request = request;
  }

  @Override
  public ResponseEntity<ClientConfigResponse> getClientConfig() {
    var context = new ClientContext(
        request.getHeader(DEVICE_OS),
        request.getHeader(DEVICE_OS_VERSION),
        request.getHeader(DEVICE_MODEL),
        request.getHeader(APP_VERSION));

    var clientConfig = clientConfigService.getClientConfig(context);

    // Values are targeted on the client headers, so shared caches must not mix responses.
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(applicationConfig.clientConfig().cacheMaxAge())
            .cachePrivate())
        .header(HttpHeaders.VARY, DEVICE_OS, DEVICE_OS_VERSION, DEVICE_MODEL, APP_VERSION)
        .body(clientConfigMapper.toResponse(clientConfig));
  }
}
