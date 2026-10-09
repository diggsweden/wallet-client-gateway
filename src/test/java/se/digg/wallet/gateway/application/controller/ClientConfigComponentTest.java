// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import se.digg.wallet.gateway.api.v0.ClientUtilsApi;
import se.digg.wallet.gateway.api.v0.model.ClientConfigResponse;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.mapper.clientconfig.ClientContextMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureRestTestClient
class ClientConfigComponentTest {

  @Autowired
  private RestTestClient client;

  @Autowired
  private ApplicationConfig applicationConfig;

  @Test
  void servesClientConfig() {
    var response = client.get()
        .uri(ClientUtilsApi.PATH_GET_CLIENT_CONFIG)
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .exchange()
        .expectStatus().isOk()
        .expectHeader().cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePrivate())
        .expectBody(ClientConfigResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(response).isNotNull();
    assertThat(response.getCacheGeneration()).isEqualTo(3);
    assertThat(response.getFeatures())
        .containsExactlyInAnyOrderEntriesOf(Map.of("example", false, "unset-feature", false));
  }

  @Test
  void targetsOnClientHeaders() {
    var response = client.get()
        .uri(ClientUtilsApi.PATH_GET_CLIENT_CONFIG)
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .header(ClientContextMapper.DEVICE_OS, "Android")
        .header(ClientContextMapper.APP_VERSION, "2.0.0")
        .exchange()
        .expectStatus().isOk()
        .expectBody(ClientConfigResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(response).isNotNull();
    assertThat(response.getFeatures()).containsEntry("example", true);
  }

  @Test
  void rejectsRequestWithoutApiKey() {
    client.get()
        .uri(ClientUtilsApi.PATH_GET_CLIENT_CONFIG)
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  void rejectsRequestWithWrongApiKey() {
    client.get()
        .uri(ClientUtilsApi.PATH_GET_CLIENT_CONFIG)
        .header(SecurityConfig.API_KEY_HEADER, "wrong")
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  void returnsNotModifiedWhenConfigUnchanged() {
    var etag = client.get()
        .uri(ClientUtilsApi.PATH_GET_CLIENT_CONFIG)
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .exchange()
        .expectStatus().isOk()
        .returnResult()
        .getResponseHeaders()
        .getETag();

    assertThat(etag).isNotBlank();

    client.get()
        .uri(ClientUtilsApi.PATH_GET_CLIENT_CONFIG)
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .header(HttpHeaders.IF_NONE_MATCH, etag)
        .exchange()
        .expectStatus().isNotModified()
        .expectBody().isEmpty();
  }

  @Test
  void returnsNewConfigWhenEtagDiffers() {
    client.get()
        .uri(ClientUtilsApi.PATH_GET_CLIENT_CONFIG)
        .header(SecurityConfig.API_KEY_HEADER, applicationConfig.apisecret())
        .header(HttpHeaders.IF_NONE_MATCH, "\"stale\"")
        .exchange()
        .expectStatus().isOk()
        .expectHeader().exists(HttpHeaders.ETAG)
        .expectBody(ClientConfigResponse.class);
  }

  @Test
  void rejectsRequestWithoutApiKeyEvenWithEtag() {
    client.get()
        .uri(ClientUtilsApi.PATH_GET_CLIENT_CONFIG)
        .header(HttpHeaders.IF_NONE_MATCH, "\"any\"")
        .exchange()
        .expectStatus().isForbidden();
  }
}
