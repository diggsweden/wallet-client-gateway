// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.exception;

public class WalletException extends RuntimeException {

  public WalletException(String message) {
    super(message);
  }

  public WalletException(String message, Throwable cause) {
    super(message, cause);
  }

  public WalletException(Throwable cause) {
    super(cause);
  }
}
