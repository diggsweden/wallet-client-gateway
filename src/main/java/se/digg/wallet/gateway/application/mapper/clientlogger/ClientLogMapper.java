// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.clientlogger;

import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.api.v0.model.ClientLogRequest;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientContext;
import se.digg.wallet.gateway.domain.model.clientlogger.ClientLogEntry;
import se.digg.wallet.gateway.domain.model.clientlogger.ClientLogEntryBuilder;

@Component
public class ClientLogMapper {

  public ClientLogEntry toClientLogEntry(ClientContext clientContext, ClientLogRequest request) {
    return ClientLogEntryBuilder.builder()
        .level(toLogLevel(request.getLevel()))
        .message(request.getMessage())
        .deviceOs(clientContext.os())
        .deviceOsVersion(clientContext.osVersion())
        .deviceModel(clientContext.deviceModel())
        .appVersion(clientContext.appVersion())
        .appBuild(clientContext.appBuild())
        .build();
  }

  private ClientLogEntry.LogLevel toLogLevel(ClientLogRequest.LevelEnum level) {
    return switch (level) {
      case ERROR -> ClientLogEntry.LogLevel.ERROR;
      case WARN -> ClientLogEntry.LogLevel.WARN;
      case INFO -> ClientLogEntry.LogLevel.INFO;
      case DEBUG -> ClientLogEntry.LogLevel.DEBUG;
      case TRACE -> ClientLogEntry.LogLevel.TRACE;
    };
  }
}
