// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.model.attestation.CreateAttestationDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(port = 0))
class AttestationControllerIntegrationTest {
  @Autowired
  private WebTestClient restClient;

  private boolean authenticated = false;

  @Value("${wiremock.server.baseUrl}")
  private String wireMockUrl;

  @Autowired
  private ApplicationConfig applicationConfig;

  @LocalServerPort
  private int port;

  UUID hsmId = UUID.randomUUID();
  UUID wuaId = UUID.randomUUID();
  UUID dbId = UUID.randomUUID();

  @BeforeEach
  public void beforeEach() throws Exception {
    if (!authenticated) {
      restClient = AuthUtil.login(port, restClient);
      authenticated = true;
    }
  }

  @Test
  void testCreateAttestation() {
    stubFor(post(applicationConfig.attributeattestation().paths().post())
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
        .bodyValue(createAttestationDto)
        .exchange();
    response.expectStatus()
        .isCreated()
        .expectBody()
        .json("""
            { "id": "%s",
                  "hsmId": "%s",
                  "wuaId": "%s",
                  "attestationData": "%s"
              }
              """.formatted(dbId, hsmId, wuaId,
            "a string"));
  }

  @Test
  void testGetAttestation() {
    stubFor(get(
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
            { "id": "%s",
                  "hsmId": "%s",
                  "wuaId": "%s",
                  "attestationData": "%s"
              }
              """.formatted(dbId, hsmId, wuaId,
            "a string"));
  }

  @Test
  void testGetAttestationByHsmId() {

    stubFor(get(
        applicationConfig.attributeattestation().paths().getByKey() + "/" + dbId.toString())

        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("content-type", "application/json")
            .withBody("""
                 {      "attestations": [
                   { "id": "%s",
                    "hsmId": "%s",
                    "wuaId": "%s",
                    "attestationData": "%s"}
                ],
                "hsmId": "%s" }
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
            {      "attestations": [
                        { "id": "%s",
                         "hsmId": "%s",
                         "wuaId": "%s",
                         "attestationData": "%s"}
                     ],
                     "hsmId": "%s" }
              """.formatted(dbId, hsmId, wuaId,
            "a string", hsmId));
  }
}
