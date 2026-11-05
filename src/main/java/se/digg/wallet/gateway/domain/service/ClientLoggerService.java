// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.domain.model.clientlogger.ClientLogEntry;
import se.digg.wallet.gateway.domain.ports.outbound.ClientLoggerPort;

@Service
public class ClientLoggerService {

  @Autowired
  private ClientLoggerPort clientLoggerPort;

  public void createClientLog(ClientLogEntry clientLogEntry) {
    clientLoggerPort.createClientLog(clientLogEntry);
  }
}
