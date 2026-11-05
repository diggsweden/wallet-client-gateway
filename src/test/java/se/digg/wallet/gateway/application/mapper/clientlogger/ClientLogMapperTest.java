// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.clientlogger;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import se.digg.wallet.gateway.api.v0.model.ClientLogRequest;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientContext;
import se.digg.wallet.gateway.domain.model.clientlogger.ClientLogEntry;
import se.digg.wallet.gateway.domain.model.clientlogger.ClientLogEntryBuilder;

class ClientLogMapperTest {

  private final static String MESSAGE = "The log message";
  private final ClientLogMapper mapper = new ClientLogMapper();

  @Test
  void maps_the_message_and_client_metadata_to_a_log_entry() {
    var context = new ClientContext("Android", "15", "Pixel 9", "1.2.3", "42");
    var request = ClientLogRequest.builder()
        .level(ClientLogRequest.LevelEnum.INFO)
        .message(MESSAGE)
        .build();

    var result = mapper.toClientLogEntry(context, request);

    assertThat(result).isEqualTo(ClientLogEntryBuilder.builder()
        .level(ClientLogEntry.LogLevel.INFO)
        .message(request.getMessage())
        .deviceOs("Android")
        .deviceOsVersion("15")
        .deviceModel("Pixel 9")
        .appVersion("1.2.3")
        .appBuild("42")
        .build());
  }

  @ParameterizedTest
  @EnumSource(ClientLogRequest.LevelEnum.class)
  void maps_each_request_log_level_to_the_corresponding_level(
      ClientLogRequest.LevelEnum level) {
    var request = ClientLogRequest.builder()
        .level(level)
        .message(MESSAGE)
        .build();

    var result = mapper.toClientLogEntry(ClientContext.empty(), request);

    assertThat(result.level()).isEqualTo(ClientLogEntry.LogLevel.valueOf(level.name()));
  }

  @Test
  void maps_missing_optional_client_metadata_to_null() {
    var request = ClientLogRequest.builder()
        .level(ClientLogRequest.LevelEnum.WARN)
        .message(MESSAGE)
        .build();

    var result = mapper.toClientLogEntry(ClientContext.empty(), request);

    assertThat(result).isEqualTo(ClientLogEntryBuilder.builder()
        .level(ClientLogEntry.LogLevel.WARN)
        .message(request.getMessage())
        .deviceOs(null)
        .deviceOsVersion(null)
        .deviceModel(null)
        .appVersion(null)
        .appBuild(null)
        .build());
  }
}
