// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static se.digg.wallet.gateway.infrastructure.clientlog.ClientLoggerAdapter.CLIENT_LOGGER_NAME;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.redis.testcontainers.RedisContainer;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.api.v0.ClientUtilsApi;
import se.digg.wallet.gateway.api.v0.model.ClientLogRequest;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.mapper.clientconfig.ClientContextMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@Testcontainers
@ActiveProfiles("test")
class ClientLoggerIntegrationTest {

  private static final String MESSAGE =
      "Wallet says: åäö, progress 100%, placeholder {} [] <> and %s";

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer accountServer;

  @Autowired
  private ApplicationConfig applicationConfig;

  private final Logger logger = (Logger) LoggerFactory.getLogger(CLIENT_LOGGER_NAME);
  private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
  private Level originalLevel;
  private RestTestClient restClient;

  @BeforeEach
  void setUp() {
    restClient = RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .defaultHeader(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .build();
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
  @EnumSource(ClientLogRequest.LevelEnum.class)
  void logs_the_message_and_client_metadata_at_the_requested_level(
      ClientLogRequest.LevelEnum level) throws Exception {
    authenticatedClient().post()
        .uri(ClientUtilsApi.PATH_CREATE_CLIENT_LOG)
        .header(ClientContextMapper.DEVICE_OS, "Android")
        .header(ClientContextMapper.DEVICE_OS_VERSION, "15")
        .header(ClientContextMapper.DEVICE_MODEL, "Pixel 9")
        .header(ClientContextMapper.APP_VERSION, "1.2.3")
        .header(ClientContextMapper.APP_BUILD, "42")
        .body(ClientLogRequest.builder().level(level).message(MESSAGE).build())
        .exchange()
        .expectStatus().isNoContent()
        .expectBody().isEmpty();

    assertThat(appender.list).singleElement().satisfies(event -> {
      assertThat(event.getLoggerName()).isEqualTo(CLIENT_LOGGER_NAME);
      assertThat(event.getLevel()).isEqualTo(Level.valueOf(level.name()));
      assertThat(event.getFormattedMessage()).contains(
          "os: Android", "osVer: 15", "model: Pixel 9", "appVer: 1.2.3", "appBuild: 42", MESSAGE);
    });
  }

  @Test
  void rejects_a_request_without_credentials() {
    RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build()
        .post()
        .uri(ClientUtilsApi.PATH_CREATE_CLIENT_LOG)
        .body(Map.of("message", MESSAGE))
        .exchange()
        .expectStatus().isForbidden();

    assertThat(appender.list).isEmpty();
  }

  private RestTestClient authenticatedClient() throws Exception {
    return AuthUtil.login(accountServer, port, restClient);
  }
}
