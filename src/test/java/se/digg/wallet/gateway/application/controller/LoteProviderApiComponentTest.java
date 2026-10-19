// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;
import se.digg.wallet.gateway.api.v0.TrustApi;
import se.digg.wallet.gateway.api.v0.model.LoteProviderItemResponse;
import se.digg.wallet.gateway.api.v0.model.LoteProvidersResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;
import se.digg.wallet.gateway.domain.model.lote.LoteProvider;
import se.digg.wallet.gateway.domain.service.LoteProviderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static se.digg.wallet.gateway.application.controller.util.ProblemDetailAssertion.assertProblemDetails;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoteProviderApiComponentTest {

  private static final String TRANSACTION_ID = "a7240655-a568-41c8-8059-7b18859d5d88";

  @MockitoBean
  private LoteProviderService loteProviderService;

  private RestTestClient client;

  @BeforeEach
  void setUp(WebApplicationContext context) {
    client = RestTestClient.bindToApplicationContext(context).build();
    MDC.put("transactionId", TRANSACTION_ID);
  }

  @Test
  void serves_configured_lote_providers() {
    var firstProvider = new LoteProvider(
        "Test 1", URI.create("https://first-url.test"));
    var secondProvider = new LoteProvider(
        "Test 2", URI.create("https://second-url.test"));
    when(loteProviderService.getLoteProviders())
        .thenReturn(List.of(firstProvider, secondProvider));

    var response = performRequest()
        .expectStatus().isOk()
        .expectBody(LoteProvidersResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(response).isNotNull();
    assertThat(response.getItems()).containsExactly(
        new LoteProviderItemResponse(firstProvider.id(), firstProvider.url()),
        new LoteProviderItemResponse(secondProvider.id(), secondProvider.url()));
  }

  @Test
  void serves_empty_list_when_no_lote_providers_are_configured() {
    when(loteProviderService.getLoteProviders()).thenReturn(List.of());

    var response = performRequest()
        .expectStatus().isOk()
        .expectBody(LoteProvidersResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(response).isNotNull();
    assertThat(response.getItems()).isEmpty();
  }

  @Test
  void informs_client_of_unexpected_server_error_problem() {
    when(loteProviderService.getLoteProviders()).thenThrow(new RuntimeException("failure"));

    var problemResponse = performRequest()
        .expectStatus().is5xxServerError()
        .expectBody(ProblemResponse.class)
        .returnResult()
        .getResponseBody();

    assertProblemDetails(problemResponse, HttpStatus.INTERNAL_SERVER_ERROR, TRANSACTION_ID);
  }

  private RestTestClient.ResponseSpec performRequest() {
    return client.get()
        .uri(TrustApi.PATH_GET_LOTE_PROVIDERS)
        .exchange();
  }
}
