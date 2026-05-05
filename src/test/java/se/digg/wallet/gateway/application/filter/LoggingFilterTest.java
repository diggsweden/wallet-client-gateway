// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.web.SpringBootMockServletContext;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({
    MockitoExtension.class,
    OutputCaptureExtension.class
})
@SpringBootTest(
    properties = {
        "properties.logging-filter.enabled=true",
        "properties.logging-filter.exclude-path.starts-with=/actuator,/exclude",
        "properties.logging-filter.exclude-path.contains=.html,.js,.yaml"
    })
public class LoggingFilterTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private LoggingFilter filter;

  @Mock
  private SensitiveDataMasker sensitiveDataMasker;

  private final MockFilterChain filterChain = new MockFilterChain();

  @Test
  void logRequest(CapturedOutput console) throws IOException, ServletException {

    var httpServletRequest = MockMvcRequestBuilders
        .get("/test")
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();

    assertThat(getLoggedRequest(consoleOut)).isNotEmpty();
  }

  @Test
  void logRequestHeaders(CapturedOutput console) throws IOException, ServletException {

    var expectedHeaderName = "ExpectedHeaderName";
    var expectedHeaderValue = "ExpectedHeaderValue";
    var httpServletRequest = MockMvcRequestBuilders
        .get("/test")
        .header(expectedHeaderName, expectedHeaderValue)
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();
    var loggedRequestHeaders = getLoggedRequestHeaders(consoleOut);

    assertThat(loggedRequestHeaders).containsKey(expectedHeaderName);
    assertThat(loggedRequestHeaders.get(expectedHeaderName)).isEqualTo(expectedHeaderValue);
  }

  @Test
  void logResponse(CapturedOutput console) throws IOException, ServletException {

    var httpServletRequest = MockMvcRequestBuilders
        .get("/test")
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();

    assertThat(getLoggedResponse(consoleOut)).isNotEmpty();
  }

  @Test
  void shouldNotLogWhenPathStartsWithExcludePattern(CapturedOutput console)
      throws IOException, ServletException {

    var httpServletRequest = MockMvcRequestBuilders
        .get("/exclude")
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilter(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();

    assertThat(getLoggedObject(consoleOut)).isEmpty();
  }

  @Test
  void shouldNotLogWhenPathContainsExcludePattern(CapturedOutput console)
      throws IOException, ServletException {

    var httpServletRequest = MockMvcRequestBuilders
        .get("/the-path/test.js")
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilter(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();

    assertThat(getLoggedObject(consoleOut)).isEmpty();
  }

  private Map getLoggedObject(String consoleOut) throws JsonProcessingException {

    var startPos = consoleOut.indexOf("{");
    if (startPos > -1
        && consoleOut.substring(startPos, consoleOut.length() - 1).contains("}")) {
      var jsonString = consoleOut.substring(startPos);
      return objectMapper.readValue(jsonString, Map.class);
    }
    return Map.of();
  }

  private Map getLoggedRequest(String consoleOut) throws JsonProcessingException {

    return (Map) getLoggedObject(consoleOut).get("request");
  }

  private Map getLoggedRequestHeaders(String consoleOut) throws JsonProcessingException {

    var loggedRequest = getLoggedRequest(consoleOut);
    return (Map) loggedRequest.get("headers");
  }

  private Map getLoggedResponse(String consoleOut) throws JsonProcessingException {

    return (Map) getLoggedObject(consoleOut).get("response");
  }
}
