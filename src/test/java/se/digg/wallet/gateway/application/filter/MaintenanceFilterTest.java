// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.ObjectMapperConfig;
import se.digg.wallet.gateway.application.controller.ProblemType;
import se.digg.wallet.gateway.domain.exception.UnavailableDueToMaintenanceException;
import se.digg.wallet.gateway.domain.service.MaintenanceService;
import se.digg.wallet.gateway.support.FakeFeatureFlagPort;

class MaintenanceFilterTest {

  private static final Instant NOW = Instant.parse("2026-10-03T12:00:00Z");

  private final FakeFeatureFlagPort flags = new FakeFeatureFlagPort();
  private final ObjectMapper objectMapper = new ObjectMapperConfig().objectMapper();
  private final MaintenanceFilter filter = new MaintenanceFilter(
      new MaintenanceService(flags, Clock.fixed(NOW, ZoneOffset.UTC)),
      (request, response, handler, ex) -> {
        if (ex instanceof UnavailableDueToMaintenanceException maintenance) {
          response.setStatus(maintenance.getStatusCode().value());
          response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
          try {
            objectMapper.writeValue(response.getOutputStream(), maintenance.getBody());
          } catch (java.io.IOException e) {
            throw new UncheckedIOException(e);
          }
          return new ModelAndView();
        }
        return null;
      },
      new ApplicationConfig.Maintenance(
          List.of("/actuator/**", "/util/v0/client-configs", "/util/v0/maintenance-windows")));

  @Test
  void passesRequestsWhenNotInMaintenance() throws Exception {
    var chain = new MockFilterChain();

    var response = filter("/v0/accounts", chain);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(chain.getRequest()).isNotNull();
  }

  @Test
  void rejectsRequestsDuringWindow() throws Exception {
    flags.values.put("gateway.maintenance.starts-at", "2026-10-03T11:00:00Z");
    flags.values.put("gateway.maintenance.ends-at", "2026-10-03T12:10:00Z");
    var chain = new MockFilterChain();

    var response = filter("/v0/accounts", chain);

    assertThat(chain.getRequest()).isNull();
    assertThat(response.getStatus()).isEqualTo(503);
    assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("601");
    assertThat(response.getHeader(HttpHeaders.CACHE_CONTROL)).isEqualTo("no-store");
    assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    var problem = objectMapper.readTree(response.getContentAsByteArray());
    assertThat(problem.get("type").asText()).isEqualTo(ProblemType.MAINTENANCE.getUri().toString());
    assertThat(problem.get("status").asInt()).isEqualTo(503);
    assertThat(problem.get("instance").asText()).isEqualTo("/v0/accounts");
  }

  @Test
  void omitsRetryAfterWhenEndIsUnknown() throws Exception {
    flags.values.put("gateway.maintenance.enabled", true);

    var response = filter("/v0/accounts", new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(503);
    assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isNull();
  }

  @Test
  void servesAllowedPathsDuringMaintenance() throws Exception {
    flags.values.put("gateway.maintenance.enabled", true);

    for (var path : List.of(
        "/util/v0/client-configs", "/util/v0/maintenance-windows", "/actuator/health")) {
      var chain = new MockFilterChain();
      filter(path, chain);
      assertThat(chain.getRequest()).as(path).isNotNull();
    }
  }

  private MockHttpServletResponse filter(String path, MockFilterChain chain) throws Exception {
    var request = new MockHttpServletRequest("GET", path);
    request.setServletPath(path);
    var response = new MockHttpServletResponse();
    filter.doFilter(request, response, chain);
    return response;
  }
}
