// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.exception;

/**
 * Thrown when an account creation request conflicts with an already existing account, e.g. when an
 * account already uses the same device key {@code kid}.
 */
public class AccountAlreadyExistsException extends WalletAccountException {

  public AccountAlreadyExistsException(String message) {
    super(message);
  }
}
