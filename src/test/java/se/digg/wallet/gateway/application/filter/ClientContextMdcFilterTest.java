// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import se.digg.wallet.gateway.application.mapper.clientconfig.ClientContextMapper;

class ClientContextMdcFilterTest {

  private final ClientContextMdcFilter filter = new ClientContextMdcFilter();

  @BeforeEach
  void setUp() {
    MDC.clear();
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  void exposes_client_headers_in_mdc_during_the_chain_and_removes_them_afterwards()
      throws Exception {
    var request = clientRequest();
    var response = new MockHttpServletResponse();

    filter.doFilter(request, response, (chainRequest, chainResponse) -> {
      assertThat(chainRequest).isSameAs(request);
      assertThat(chainResponse).isSameAs(response);
      assertThat(MDC.getCopyOfContextMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
          "client.os.name", "Android",
          "client.os.version", "15",
          "client.device.model.name", "Pixel 9",
          "client.app.version", "2.1.0",
          "client.app.build", "40"));
      chainResponse.getWriter().write("response");
    });

    assertThat(response.getContentAsString()).isEqualTo("response");
    assertClientMdcIsEmpty();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t", "\r\n"})
  void omits_missing_or_blank_headers(String value) throws Exception {
    var request = new MockHttpServletRequest();
    if (value != null) {
      ClientContextMapper.VARY_HEADERS.forEach(header -> request.addHeader(header, value));
    }
    var response = new MockHttpServletResponse();

    filter.doFilter(request, response, (chainRequest, chainResponse) -> {
      assertClientMdcIsEmpty();
      chainResponse.getWriter().write("chain invoked");
    });

    assertThat(response.getContentAsString()).isEqualTo("chain invoked");
    assertClientMdcIsEmpty();
  }

  @Test
  void populates_only_the_headers_that_are_present() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader(ClientContextMapper.DEVICE_OS, "Android");

    filter.doFilter(request, new MockHttpServletResponse(), (chainRequest, chainResponse) -> {
      assertThat(MDC.getCopyOfContextMap())
          .containsExactlyInAnyOrderEntriesOf(Map.of("client.os.name", "Android"));
    });

    assertClientMdcIsEmpty();
  }

  @Test
  void strips_carriage_returns_and_newlines_from_all_client_values() throws Exception {
    var request = new MockHttpServletRequest();
    ClientContextMapper.VARY_HEADERS
        .forEach(header -> request.addHeader(header, "\rAnd\nroid\r\n"));

    filter.doFilter(request, new MockHttpServletResponse(), (chainRequest, chainResponse) -> {
      ClientContextMdcFilter.TRACKED_KEYS
          .forEach(key -> assertThat(MDC.get(key)).as(key).isEqualTo("Android"));
    });

    assertClientMdcIsEmpty();
  }

  @Test
  void preserves_unrelated_mdc_entries() throws Exception {
    MDC.put("transactionId", "transaction-123");

    filter.doFilter(clientRequest(), new MockHttpServletResponse(), (request, response) -> {
      assertThat(MDC.get("transactionId")).isEqualTo("transaction-123");
      MDC.put("downstream", "retained");
    });

    assertThat(MDC.getCopyOfContextMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
        "transactionId", "transaction-123", "downstream", "retained"));
  }

  @ParameterizedTest
  @MethodSource("chainFailures")
  void removes_client_mdc_and_preserves_unrelated_entries_when_the_chain_throws(Exception failure) {
    MDC.put("transactionId", "transaction-123");

    assertThatThrownBy(() -> filter.doFilter(
        clientRequest(), new MockHttpServletResponse(), (request, response) -> {
          assertThat(MDC.get("client.os.name")).isEqualTo("Android");
          if (failure instanceof IOException ioException) {
            throw ioException;
          }
          if (failure instanceof ServletException servletException) {
            throw servletException;
          }
          throw (RuntimeException) failure;
        })).isSameAs(failure);

    assertThat(MDC.getCopyOfContextMap())
        .containsExactlyInAnyOrderEntriesOf(Map.of("transactionId", "transaction-123"));
  }

  @Test
  void does_not_leak_client_values_into_the_next_request_on_the_same_thread() throws Exception {
    filter.doFilter(clientRequest(), new MockHttpServletResponse(), (request, response) -> {
      assertThat(MDC.get("client.os.name")).isEqualTo("Android");
    });

    filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
        (request, response) -> assertClientMdcIsEmpty());
  }

  private static Stream<Exception> chainFailures() {
    return Stream.of(
        new IOException("I/O failure"),
        new ServletException("Servlet failure"),
        new IllegalStateException("Runtime failure"));
  }

  private static MockHttpServletRequest clientRequest() {
    var request = new MockHttpServletRequest();
    request.addHeader(ClientContextMapper.DEVICE_OS, "Android");
    request.addHeader(ClientContextMapper.DEVICE_OS_VERSION, "15");
    request.addHeader(ClientContextMapper.DEVICE_MODEL, "Pixel 9");
    request.addHeader(ClientContextMapper.APP_VERSION, "2.1.0");
    request.addHeader(ClientContextMapper.APP_BUILD, "40");
    return request;
  }

  private static void assertClientMdcIsEmpty() {
    ClientContextMdcFilter.TRACKED_KEYS
        .forEach(key -> assertThat(MDC.get(key)).as(key).isNull());
  }
}
