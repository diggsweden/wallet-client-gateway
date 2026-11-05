// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.ports.outbound;

import se.digg.wallet.gateway.domain.model.clientlogger.ClientLogEntry;

public interface ClientLoggerPort {

  void createClientLog(ClientLogEntry clientLogEntry);
}
