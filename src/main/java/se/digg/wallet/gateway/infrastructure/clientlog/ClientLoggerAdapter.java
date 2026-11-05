// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.clientlog;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.domain.model.clientlogger.ClientLogEntry;
import se.digg.wallet.gateway.domain.ports.outbound.ClientLoggerPort;

@Component
public class ClientLoggerAdapter implements ClientLoggerPort {

  public static final String CLIENT_LOGGER_NAME = "WalletClientApp";
  private static final Logger logger = LoggerFactory.getLogger(CLIENT_LOGGER_NAME);

  @Override
  public void createClientLog(ClientLogEntry clientLogEntry) {
    final var logMessage = composeLogMessage(clientLogEntry);
    switch (clientLogEntry.level()) {
      case ERROR -> logger.error(logMessage);
      case WARN -> logger.warn(logMessage);
      case INFO -> logger.info(logMessage);
      case DEBUG -> logger.debug(logMessage);
      case TRACE -> logger.trace(logMessage);
    }
  }

  private String composeLogMessage(ClientLogEntry clientLogEntry) {

    return Optional.ofNullable(clientLogEntry.deviceOs()).map("os: %s, "::formatted).orElse("") +
        Optional.ofNullable(clientLogEntry.deviceOsVersion()).map("osVer: %s, "::formatted)
            .orElse("")
        +
        Optional.ofNullable(clientLogEntry.deviceModel()).map("model: %s, "::formatted).orElse("") +
        Optional.ofNullable(clientLogEntry.appVersion()).map("appVer: %s, "::formatted).orElse("") +
        Optional.ofNullable(clientLogEntry.appBuild()).map("appBuild: %s, "::formatted).orElse("") +
        clientLogEntry.message();
  }
}
