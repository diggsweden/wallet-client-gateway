// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.clientconfig;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientContext;

public final class ClientContextMapper {

  public static final String DEVICE_OS = "Wallet-Device-OS";
  public static final String DEVICE_OS_VERSION = "Wallet-Device-OS-Version";
  public static final String DEVICE_MODEL = "Wallet-Device-Model";
  public static final String APP_VERSION = "Wallet-App-Version";

  public static final List<String> VARY_HEADERS = List.of(
      DEVICE_OS,
      DEVICE_OS_VERSION,
      DEVICE_MODEL,
      APP_VERSION);

  private ClientContextMapper() {
  }

  public static ClientContext from(HttpServletRequest request) {
    return new ClientContext(
        request.getHeader(DEVICE_OS),
        request.getHeader(DEVICE_OS_VERSION),
        request.getHeader(DEVICE_MODEL),
        request.getHeader(APP_VERSION));
  }
}
