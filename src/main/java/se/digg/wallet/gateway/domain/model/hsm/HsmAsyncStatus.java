// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.hsm;

/*
 * Enum constants reflecting the asynchronous workflow status of an HSM request.
 */
public enum HsmAsyncStatus {

  COMPLETE,
  PENDING,
  ERROR;

  public static HsmAsyncStatus fromValue(String value) {
    for (HsmAsyncStatus s : values()) {
      if (s.name().equalsIgnoreCase(value)) {
        return s;
      }
    }
    throw new IllegalArgumentException("Unexpected enum value '" + value + "'");
  }
}
