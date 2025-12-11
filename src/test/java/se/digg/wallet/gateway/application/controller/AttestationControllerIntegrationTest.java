// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.redis.testcontainers.RedisContainer;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.InjectWireMock;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.controller.util.AuthUtil;
import se.digg.wallet.gateway.application.controller.util.WalletAccountMock;
import se.digg.wallet.gateway.application.controller.util.WalletAttributeAttestationMock;
import se.digg.wallet.gateway.application.model.attestation.CreateAttestationDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WalletAccountMock
@WalletAttributeAttestationMock
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureRestTestClient
class AttestationControllerIntegrationTest {


  @Container
  @ServiceConnection
  static RedisContainer redisContainer = RedisTestConfiguration.redisContainer();

  private RestTestClient restClient;

  private boolean authenticated = false;

  @Value("${wiremock.server.baseUrl}")
  private String wireMockUrl;

  @Autowired
  private ApplicationConfig applicationConfig;

  @LocalServerPort
  private int port;

  @InjectWireMock(WalletAccountMock.NAME)
  private WireMockServer accountServer;

  @InjectWireMock(WalletAttributeAttestationMock.NAME)
  private WireMockServer attestationServer;

  UUID hsmId = UUID.randomUUID();
  UUID wuaId = UUID.randomUUID();
  UUID dbId = UUID.randomUUID();

  @BeforeEach
  public void beforeEach() throws Exception {
    if (!authenticated) {
      restClient = RestTestClient.bindToServer()
          .baseUrl("http://localhost:" + port)
          .build();
      restClient = AuthUtil.login(accountServer, port, restClient);
      authenticated = true;
    }
  }

  @Test
  void testCreateAttestation() {
    attestationServer.stubFor(post(applicationConfig.attributeattestation().paths().post())
        .withRequestBody(equalToJson("""
            {
                "id": null,
                "hsmId": "%s",
                "wuaId": "%s",
                "attestationData": "%s"
            }
            """.formatted(hsmId, wuaId, "a string")))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                    "id": "%s",
                    "hsmId": "%s",
                    "wuaId": "%s",
                    "attestationData": "%s"
                }
                """.formatted(dbId, hsmId,
                wuaId, "a string"))));
    CreateAttestationDto createAttestationDto =
        new CreateAttestationDto(hsmId, wuaId, "a string");

    var response = restClient.post()
        .uri("/attribute-attestations")
        .body(createAttestationDto)
        .exchange();
    response.expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            {
                "id": "%s",
                "hsmId": "%s",
                "wuaId": "%s",
                "attestationData": "%s"
            }
              """.formatted(dbId, hsmId, wuaId,
            "a string"));
  }

  @Test
  void testGetAttestation() {
    attestationServer.stubFor(get(
        applicationConfig.attributeattestation().paths().getById() + "/" + dbId.toString())
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "application/json")
            .withBody("""
                {
                    "id": "%s",
                    "hsmId": "%s",
                    "wuaId": "%s",
                    "attestationData": "%s"
                }
                """.formatted(dbId, hsmId,
                wuaId, "a string"))));

    var response = restClient.get()
        .uri("/attribute-attestations/" + dbId.toString())
        .exchange();
    response.expectStatus()
        .isOk()
        .expectBody()
        .json("""
            {
                "id": "%s",
                "hsmId": "%s",
                "wuaId": "%s",
                "attestationData": "%s"
            }
              """.formatted(dbId, hsmId, wuaId,
            "a string"));
  }

  @Test
  void testGetAttestationByHsmId() {
    attestationServer.stubFor(get(
        applicationConfig.attributeattestation().paths().getByKey() + "/" + dbId.toString())

        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "application/json")
            .withBody("""
                 {
                    "attestations": [
                        {
                            "id": "%s",
                            "hsmId": "%s",
                            "wuaId": "%s",
                            "attestationData": "%s"
                        }
                    ],
                    "hsmId": "%s"
                }
                """.formatted(dbId, hsmId,
                wuaId, "a string", hsmId))));

    var response = restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/attribute-attestations")
            .queryParam("key", dbId.toString())
            .build())
        .exchange();
    response.expectStatus()
        .isOk()
        .expectBody()
        .json("""
            {
                "attestations": [
                    {
                        "id": "%s",
                        "hsmId": "%s",
                        "wuaId": "%s",
                        "attestationData": "%s"
                    }
                ],
                "hsmId": "%s"
            }
            """.formatted(dbId, hsmId, wuaId,
            "a string", hsmId));
  }
}
