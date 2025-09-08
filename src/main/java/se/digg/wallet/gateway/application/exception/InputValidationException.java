// SPDX-FileCopyrightText: 2025 diggsweden/wallet-backend-reference
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.exception;

import java.util.Objects;

public class InputValidationException extends RuntimeException {

  private final Error title;
  private final String detail;

  public InputValidationException(Error title, String detail) {
    this.title = Objects.requireNonNull(title);
    this.detail = Objects.requireNonNull(detail);
  }

  public Error title() {
    return title;
  }

  public String detail() {
    return detail;
  }

  public enum Error {
    INVALID_ATTRIBUTE_ID("AttributeID is invalid");

    private final String description;

    Error(String description) {
      this.description = description;
    }

    public String description() {
      return description;
    }
  }
}
