// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.clientlogger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.LoggerFactory;
import se.digg.wallet.gateway.domain.model.clientlogger.ClientLogEntry;
import se.digg.wallet.gateway.domain.model.clientlogger.ClientLogEntryBuilder;
import se.digg.wallet.gateway.infrastructure.clientlog.ClientLoggerAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static se.digg.wallet.gateway.infrastructure.clientlog.ClientLoggerAdapter.CLIENT_LOGGER_NAME;

public class ClientLoggerAdapterTest {

  private final static String MESSAGE = "The log message";

  private final ClientLoggerAdapter clientLogAdapter = new ClientLoggerAdapter();
  private final Logger logger = (Logger) LoggerFactory.getLogger(CLIENT_LOGGER_NAME);
  private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
  private Level originalLevel;

  @BeforeEach
  void setUp() {
    originalLevel = logger.getLevel();
    logger.setLevel(Level.TRACE);
    appender.start();
    logger.addAppender(appender);
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(appender);
    appender.stop();
    logger.setLevel(originalLevel);
  }

  @ParameterizedTest
  @EnumSource(ClientLogEntry.LogLevel.class)
  void logs_the_message_and_metadata_at_the_requested_level(ClientLogEntry.LogLevel level) {
    var logEntry = ClientLogEntryBuilder.builder()
        .level(level)
        .message(MESSAGE)
        .deviceOs("Android")
        .deviceOsVersion("15")
        .deviceModel("Pixel 9")
        .appVersion("1.2.3")
        .appBuild("42")
        .build();

    clientLogAdapter.createClientLog(logEntry);

    assertThat(appender.list).singleElement().satisfies(event -> {
      assertThat(event.getLoggerName()).isEqualTo(CLIENT_LOGGER_NAME);
      assertThat(event.getLevel()).isEqualTo(Level.valueOf(level.name()));
      assertThat(event.getFormattedMessage()).contains(logEntry.message());
      assertThat(event.getFormattedMessage()).contains(logEntry.deviceOs());
      assertThat(event.getFormattedMessage()).contains(logEntry.deviceOsVersion());
      assertThat(event.getFormattedMessage()).contains(logEntry.deviceModel());
      assertThat(event.getFormattedMessage()).contains(logEntry.appVersion());
      assertThat(event.getFormattedMessage()).contains(logEntry.appBuild());
    });
  }

  @Test
  void logs_the_message_when_optional_metadata_is_missing() {
    var logEntry = ClientLogEntryBuilder.builder()
        .level(ClientLogEntry.LogLevel.INFO)
        .message(MESSAGE)
        .build();

    clientLogAdapter.createClientLog(logEntry);

    assertThat(appender.list).singleElement().satisfies(event -> {
      assertThat(event.getLevel()).isEqualTo(Level.INFO);
      assertThat(event.getFormattedMessage()).isEqualTo(MESSAGE);
    });
  }

  @Test
  void preserves_special_characters_in_the_message() {
    var logEntry = ClientLogEntryBuilder.builder()
        .level(ClientLogEntry.LogLevel.INFO)
        .message("Wallet says: åäö, progress 100%, placeholder {} [] <> and %s")
        .build();

    clientLogAdapter.createClientLog(logEntry);

    assertThat(appender.list).singleElement()
        .satisfies(event -> assertThat(event.getFormattedMessage()).isEqualTo(logEntry.message()));
  }

  @Test
  void does_not_log_a_message_below_the_configured_log_level() {
    logger.setLevel(Level.INFO);
    var logEntry = ClientLogEntryBuilder.builder()
        .level(ClientLogEntry.LogLevel.DEBUG)
        .message(MESSAGE)
        .build();

    clientLogAdapter.createClientLog(logEntry);

    assertThat(appender.list).isEmpty();
  }
}
