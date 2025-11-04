// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.exception;

import java.util.Objects;

public class BadRequestException extends RuntimeException {

  private String detail;

  public BadRequestException(String detail) {
    super(detail);
    this.detail = Objects.requireNonNull(detail);
  }

  public String detail() {
    return detail;
  }
}
