// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static se.digg.wallet.gateway.api.v0.model.HsmRequestStatus.COMPLETE;
import static se.digg.wallet.gateway.api.v0.model.HsmRequestStatus.PENDING;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.jwk.ECKey;
import com.redis.testcontainers.RedisContainer;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import se.digg.wallet.gateway.api.v0.model.HsmRequest;
import se.digg.wallet.gateway.api.v0.model.HsmResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.RedisTestConfiguration;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.controller.util.WalletR2psMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@WalletR2psMock
@Testcontainers
@ActiveProfiles("test")
class HsmControllerIntegrationTest {

  private static final String REGISTER_STATE_URL = "/hsm/v0/device-states";
  private static final String HSM_REQUESTS_URL = "/hsm/v0/requests";
  private static final String TEST_JWT = "eyJhbGciOiJFUzI1NiJ9.test.signature";
  private static final String TEST_DEV_AUTH_CODE = "test-dev-auth-code";
  private static final String ISO_8601_DURATION_30_DAYS = "P30D";

  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  private static ECKey generatedKeyPair;
  private static final String ACCOUNT_ID = UUID.randomUUID().toString();

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer accountServer;

  @InjectWireMock(WalletR2psMock.NAME)
  private WireMockServer r2psServer;

  @Autowired
  private ApplicationConfig applicationConfig;

  private RestTestClient restClient;
  private boolean authenticated = false;
  private String requestId = null;
  private String clientId = null;

  @BeforeAll
  static void beforeAll() throws Exception {
    generatedKeyPair = AuthUtil.generateKey();
  }

  @BeforeEach
  void beforeEach() throws Exception {
    if (!authenticated) {
      restClient = RestTestClient.bindToServer()
          .baseUrl("http://localhost:" + port)
          .build();
      restClient = AuthUtil.login(accountServer, port, restClient, ACCOUNT_ID, generatedKeyPair);
      restClient = restClient.mutate()
          .defaultHeader(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
          .build();
      authenticated = true;
      requestId = UUID.randomUUID().toString();
      clientId = UUID.randomUUID().toString();
    }
  }

  @Test
  void rejectsUnauthenticatedDeviceStateRegistration() {
    RestTestClient unauthenticated = RestTestClient.bindToServer()
        .baseUrl("http://localhost:" + port)
        .build();

    unauthenticated.post()
        .uri(REGISTER_STATE_URL)
        .header("content-type", "application/json")
        .body(se.digg.wallet.gateway.api.v0.model.RegisterStateRequest.builder()
            .deviceKey(se.digg.wallet.gateway.api.v0.model.KeyRequest.builder()
                .kty("EC")
                .crv("P-256")
                .x("x")
                .y("y")
                .kid("kid")
                .build())
            .overwrite(false)
            .ttl(ISO_8601_DURATION_30_DAYS)
            .build())
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void registersDeviceState() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/device-states")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "status": "ok",
                  "clientId": "%s",
                  "devAuthorizationCode": "%s",
                  "serverJwsPublicKey": {
                    "kid": "kid",
                    "kty": "EC",
                    "crv": "P-256",
                    "x": "x",
                    "y": "y"
                  },
                  "opaqueServerId": "server"
                }
                """.formatted(clientId, TEST_DEV_AUTH_CODE))));

    var registerStateRequest = se.digg.wallet.gateway.api.v0.model.RegisterStateRequest.builder()
        .clientId(clientId)
        .deviceKey(se.digg.wallet.gateway.api.v0.model.KeyRequest.builder()
            .kty("EC")
            .crv("P-256")
            .x("x")
            .y("y")
            .kid("kid")
            .build())
        .overwrite(false)
        .ttl(ISO_8601_DURATION_30_DAYS)
        .build();

    restClient.post()
        .uri(REGISTER_STATE_URL)
        .header("content-type", "application/json")
        .body(registerStateRequest)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .json(
            """
                {
                  "status":"ok",
                  "clientId":"%s",
                  "devAuthorizationCode":"%s",
                  "opaqueServerId":"server",
                  "serverJwsPublicKey":{"kty":"EC","kid":"kid","crv":"P-256","x":"x","y":"y","alg":null,"use":null}
                }
                """
                .formatted(clientId, TEST_DEV_AUTH_CODE));
  }

  @Test
  void validationFailedWithUndefinedQueryParamEnum() {
    final var paramType = "type";
    final var unknownType = "NON_EXISTING_TYPE";

    var problemResponse = restClient.post()
        .uri(HSM_REQUESTS_URL + "?" + paramType + "=" + unknownType)
        .header("content-type", "application/json")
        .body(HsmRequest.builder()
            .outerRequestJws(TEST_JWT)
            .clientId(clientId)
            .build())
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(problemResponse).isNotNull();
    assertThat(problemResponse.getTitle()).isNotEmpty();
    assertThat(problemResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problemResponse.getDetail()).hasValueSatisfying(detail -> {
      assertThat(detail).contains(paramType);
      assertThat(detail).contains(unknownType);
    });
  }

  @Test
  void badRequestWithRemoteService() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/device-states")
        .willReturn(aResponse()
            .withStatus(400)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "type": "about:blank",
                  "title": "Bad request",
                  "status": 400,
                  "detail": "Detailed message",
                  "instance": "/hsm/v1/device-states"
                }
                """)));

    var registerStateRequest = se.digg.wallet.gateway.api.v0.model.RegisterStateRequest.builder()
        .clientId(clientId)
        .deviceKey(se.digg.wallet.gateway.api.v0.model.KeyRequest.builder()
            .kty("EC")
            .crv("P-256")
            .x("x")
            .y("y")
            .kid("kid")
            .build())
        .overwrite(false)
        .ttl(ISO_8601_DURATION_30_DAYS)
        .build();

    var problemResponse = restClient.post()
        .uri(REGISTER_STATE_URL)
        .header("content-type", "application/json")
        .body(registerStateRequest)
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(problemResponse).isNotNull();
    assertThat(problemResponse.getTitle()).isNotEmpty();
    assertThat(problemResponse.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(problemResponse.getDetail()).isNotEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"30d", "30"})
  void validationFailureWithBadTtlFormat(String ttl) {

    var registerStateRequest = se.digg.wallet.gateway.api.v0.model.RegisterStateRequest.builder()
        .clientId(clientId)
        .deviceKey(se.digg.wallet.gateway.api.v0.model.KeyRequest.builder()
            .kty("EC")
            .crv("P-256")
            .x("x")
            .y("y")
            .kid("kid")
            .build())
        .overwrite(false)
        .ttl(ttl)
        .build();

    var problemResponse = restClient.post()
        .uri(REGISTER_STATE_URL)
        .header("content-type", "application/json")
        .body(registerStateRequest)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(problemResponse).isNotNull();
    assertThat(problemResponse.getType()).isNotEmpty();
    var invalidParameters = problemResponse.getInvalidParameters();
    assertThat(invalidParameters).isNotEmpty().hasSize(1);
    var invalidProperty = invalidParameters.get(0).getProperty();
    assertThat(invalidProperty).isNotEmpty().get().isEqualTo("ttl");
  }

  @Test
  void completeHsmRequestCreated() {
    r2psServer.stubFor(WireMock.post("/hsm/v1/requests")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "correlationId": "%s",
                  "devAuthorizationCode": "any-string",
                  "opaqueServerId": "another-string",
                  "status": "complete",
                  "result": "%s",
                  "resultUrl": null
                }
                """.formatted(requestId, TEST_JWT))));

    restClient.post()
        .uri(HSM_REQUESTS_URL)
        .header("content-type", "application/json")
        .body(HsmRequest.builder()
            .outerRequestJws(TEST_JWT)
            .clientId(clientId)
            .build())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json("""
            {
              "status": "COMPLETE",
              "id": "%s",
              "result": "%s",
              "resultUrl": null
            }
            """.formatted(requestId, TEST_JWT));
  }

  @Test
  void pendingHsmRequestCreated() {
    var resultUrl = "/the/result/url";
    r2psServer.stubFor(WireMock.post("/hsm/v1/requests")
        .willReturn(aResponse()
            .withStatus(202)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "correlationId": "%s",
                  "devAuthorizationCode": "any-string",
                  "opaqueServerId": "another-string",
                  "status": "pending",
                  "result": null,
                  "resultUrl": "/the/result/url"
                }
                """.formatted(requestId))));

    var hsmResponse = restClient.post()
        .uri(HSM_REQUESTS_URL)
        .header("content-type", "application/json")
        .body(HsmRequest.builder()
            .outerRequestJws(TEST_JWT)
            .clientId(clientId)
            .build())
        .exchange()
        .expectStatus()
        .isAccepted()
        .expectBody(HsmResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(hsmResponse.getId()).isNotEmpty().get().isEqualTo(requestId);
    assertThat(hsmResponse.getStatus()).isEqualTo(PENDING);
    assertThat(hsmResponse.getResult()).isEmpty();
    assertThat(hsmResponse.getResultUrl()).hasValueSatisfying(url -> {
      assertThat(url).startsWith("http");
      assertThat(url).endsWith("/" + requestId);
    });
  }

  @Test
  void verifyResultUrlOnCreatedPendingHsmRequest() {
    var mockResponse = """
        {
          "correlationId": "%s",
          "devAuthorizationCode": "any-string",
          "opaqueServerId": "another-string",
          "status": "pending",
          "result": null,
          "resultUrl": "/the/result/url"
        }
        """.formatted(requestId);
    r2psServer.stubFor(WireMock.post("/hsm/v1/requests")
        .willReturn(aResponse()
            .withStatus(202)
            .withHeader("content-type", "application/json")
            .withBody(mockResponse)));
    r2psServer.stubFor(WireMock.get("/hsm/v1/requests/" + requestId)
        .willReturn(aResponse()
            .withStatus(202)
            .withHeader("content-type", "application/json")
            .withBody(mockResponse)));

    var hsmResponse = restClient.post()
        .uri(HSM_REQUESTS_URL)
        .header("content-type", "application/json")
        .body(HsmRequest.builder()
            .outerRequestJws(TEST_JWT)
            .clientId(clientId)
            .build())
        .exchange()
        .expectStatus()
        .isAccepted()
        .expectBody(HsmResponse.class)
        .returnResult()
        .getResponseBody();

    URI resultUrl = URI.create(hsmResponse.getResultUrl().orElseThrow());
    var resultUrlHsmResponse = restClient.get()
        .uri(resultUrl)
        .exchange()
        .expectStatus()
        .isAccepted()
        .expectBody(HsmResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(resultUrlHsmResponse.getId()).isNotEmpty().get().isEqualTo(requestId);
    assertThat(resultUrlHsmResponse.getStatus()).isEqualTo(PENDING);
    assertThat(resultUrlHsmResponse.getResult()).isEmpty();
  }

  @Test
  void pendingHsmRequestPolling() {
    r2psServer.stubFor(WireMock.get("/hsm/v1/requests/" + requestId)
        .willReturn(aResponse()
            .withStatus(202)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "correlationId": "%s",
                  "status": "pending",
                  "resultUrl": "/the/result/url"
                }
                """.formatted(requestId))));

    var hsmResponse = restClient.get()
        .uri(HSM_REQUESTS_URL + "/" + requestId)
        .exchange()
        .expectStatus()
        .isAccepted()
        .expectBody(HsmResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(hsmResponse.getId()).isNotEmpty().get().isEqualTo(requestId);
    assertThat(hsmResponse.getStatus()).isEqualTo(PENDING);
    assertThat(hsmResponse.getResult()).isEmpty();
    assertThat(hsmResponse.getResultUrl())
        .hasValueSatisfying(url -> {
          assertThat(url).startsWith("http");
          assertThat(url).endsWith("/" + requestId);
        });
  }

  @Test
  void completedHsmRequestPolling() {
    r2psServer.stubFor(WireMock.get("/hsm/v1/requests/" + requestId)
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                  "correlationId": "%s",
                  "status": "complete",
                  "result": "%s"
                }
                """.formatted(requestId, TEST_JWT))));

    var hsmResponse = restClient.get()
        .uri(HSM_REQUESTS_URL + "/" + requestId)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(HsmResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(hsmResponse.getId()).isNotEmpty().get().isEqualTo(requestId);
    assertThat(hsmResponse.getStatus()).isEqualTo(COMPLETE);
    assertThat(hsmResponse.getResult()).isNotEmpty();
  }

  @Test
  void nonExistingHsmRequestPolling() {
    r2psServer.stubFor(WireMock.get("/hsm/v1/requests/" + requestId)
        .willReturn(aResponse()
            .withStatus(404)
            .withHeader("content-type", "application/json")
            .withBody("")));

    var problemResponse = restClient.get()
        .uri(HSM_REQUESTS_URL + "/" + requestId)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(problemResponse).isNotNull();
    assertThat(problemResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(problemResponse.getTitle()).isNotEmpty();
  }
}
