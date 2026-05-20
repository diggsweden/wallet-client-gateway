package se.digg.wallet.gateway.application.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.jwk.ECKey;
import com.redis.testcontainers.RedisContainer;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.model.KeyRequestTestBuilder;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountAccountDtoBuilder;
import tools.jackson.databind.ObjectMapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@Testcontainers
@ActiveProfiles("test")
public class DefaultExceptionHandlerTest {

  private static final String TYPE = "type";
  private static final String STATUS = "status";

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  private static final String ACCOUNT_ID = UUID.randomUUID().toString();
  private static ECKey generatedKeyPair;

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer accountServer;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ApplicationConfig applicationConfig;

  private RestTestClient restClient;
  private boolean authenticated = false;

  @BeforeAll
  public static void beforeAll() throws Exception {
    generatedKeyPair = AuthUtil.generateKey();
  }

  @BeforeEach
  public void beforeEach() throws Exception {
    if (!authenticated) {
      restClient = RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build();
      restClient = AuthUtil.login(accountServer, port, restClient, ACCOUNT_ID, generatedKeyPair);
      authenticated = true;
    }
  }

  @Test
  void missingRequiredParameter() {

    var response = restClient.get()
      .uri("/public/auth/session/challenge")
      .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
      .exchange();

    var badRequest = HttpStatus.BAD_REQUEST.value();
    response.expectStatus().isEqualTo(badRequest);
    response.expectBody().jsonPath(TYPE)
      .isEqualTo(ProblemType.CLIENT_GENERIC.getUri().toASCIIString());
    response.expectBody().jsonPath(STATUS).isEqualTo(badRequest);
  }

  @Test
  void invalidRequestBodyFieldValue() {

    var requestBodyWithMissingProperty = WalletAccountAccountDtoBuilder.builder().build();

    var response = restClient.post()
      .uri("/v0/accounts")
      .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
      .body(requestBodyWithMissingProperty)
      .exchange();

    var badRequest = HttpStatus.BAD_REQUEST.value();
    response.expectStatus().isEqualTo(badRequest);
    response.expectBody().jsonPath(TYPE)
        .isEqualTo(ProblemType.FIELD_VALIDATION_FAILURE.getUri().toASCIIString());
    response.expectBody().jsonPath(STATUS).isEqualTo(badRequest);
  }

  @Test
  void remoteServiceFailure() {
    accountServer.stubFor(post("/v0/accounts/" + ACCOUNT_ID + "/wallet-keys")
        .willReturn(aResponse().withStatus(500)));

    var response = restClient.post()
        .uri("/v0/accounts/wallet-keys")
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .body(KeyRequestTestBuilder.withDefaults().build())
        .exchange();

    var internalServerError = HttpStatus.INTERNAL_SERVER_ERROR.value();
    response.expectStatus().isEqualTo(internalServerError);
    response.expectBody().jsonPath(TYPE)
      .isEqualTo(ProblemType.GENERIC_INTERNAL.getUri().toASCIIString());
    response.expectBody().jsonPath(STATUS).isEqualTo(internalServerError);
  }
}
