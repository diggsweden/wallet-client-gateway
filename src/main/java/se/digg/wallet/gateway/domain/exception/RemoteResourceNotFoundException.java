// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.exception;

public class RemoteResourceNotFoundException extends WalletException {

  public RemoteResourceNotFoundException(String message) {
    super(message);
  }
}
