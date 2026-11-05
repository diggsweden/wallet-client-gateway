// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.clientlogger;

import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotBlank;

@RecordBuilder
public record ClientLogEntry(
    LogLevel level,
    @NotBlank String message,
    String deviceOs,
    String deviceOsVersion,
    String deviceModel,
    String appVersion,
    String appBuild) {

  public enum LogLevel {
    ERROR, WARN, INFO, DEBUG, TRACE
  }
}
