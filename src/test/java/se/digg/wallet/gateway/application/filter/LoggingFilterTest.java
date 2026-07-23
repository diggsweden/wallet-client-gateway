// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.web.SpringBootMockServletContext;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith({
    MockitoExtension.class,
    OutputCaptureExtension.class
})
@SpringBootTest(
    properties = {
        "properties.logging-filter.enabled=true",
        "properties.logging-filter.exclude-path.exact-match=/,/exclude",
        "properties.logging-filter.exclude-path.contains=/actuator,.html"
    })
public class LoggingFilterTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private LoggingFilter filter;

  @MockitoBean
  private SensitiveDataMasker sensitiveDataMasker;

  @Mock
  private MockFilterChain filterChain;

  @Test
  void logContainsRequest(CapturedOutput console) throws IOException, ServletException {

    var httpServletRequest = MockMvcRequestBuilders
        .get("/test")
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();

    assertThat(getLoggedRequest(consoleOut)).isNotEmpty();
  }

  @Test
  void logContainsRequestHeaders(CapturedOutput console) throws IOException, ServletException {

    var expectedHeaderName = "ExpectedHeaderName";
    var expectedHeaderValue = "ExpectedHeaderValue";
    var httpServletRequest = MockMvcRequestBuilders
        .get("/test")
        .header(expectedHeaderName, expectedHeaderValue)
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    when(sensitiveDataMasker.maskHeaders(any())).thenReturn(
        Map.of(expectedHeaderName, expectedHeaderValue));

    filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();
    var loggedRequestHeaders = getLoggedRequestHeaders(consoleOut);

    assertThat(loggedRequestHeaders).containsKey(expectedHeaderName);
    assertThat(loggedRequestHeaders.get(expectedHeaderName)).isEqualTo(expectedHeaderValue);
  }

  @Test
  void logContainsResponse(CapturedOutput console) throws IOException, ServletException {

    var httpServletRequest = MockMvcRequestBuilders
        .get("/test")
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();

    assertThat(getLoggedResponse(consoleOut)).isNotEmpty();
  }

  @Test
  void shouldGenerateCorrelationIdWhenXCorrelationIdIsEmpty(CapturedOutput console)
      throws IOException, ServletException {

    var httpServletRequest = MockMvcRequestBuilders
        .get("/test")
        .header("X-Correlation-ID", "")
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();
    var loggedObject = getLoggedObject(consoleOut);

    assertThat(loggedObject).containsKey("id");
    assertThat(loggedObject.get("id").toString()).hasSizeGreaterThan(1);
  }

  @Test
  void shouldGenerateCorrelationIdWhenXRequestIdIsEmpty(CapturedOutput console)
      throws IOException, ServletException {

    var httpServletRequest = MockMvcRequestBuilders
        .get("/test")
        .header("X-Request-ID", "")
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();
    var loggedObject = getLoggedObject(consoleOut);

    assertThat(loggedObject).containsKey("id");
    assertThat(loggedObject.get("id").toString()).hasSizeGreaterThan(1);
  }

  @Test
  void logGivenXCorrelationId(CapturedOutput console) throws IOException, ServletException {

    var expectedId = randomId();
    var httpServletRequest = MockMvcRequestBuilders
        .get("/test")
        .header("X-Correlation-ID", expectedId)
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();
    var loggedObject = getLoggedObject(consoleOut);

    assertThat(loggedObject).containsKey("id");
    assertThat(loggedObject.get("id")).isEqualTo(expectedId);
  }

  @Test
  void logGivenXRequestId(CapturedOutput console) throws IOException, ServletException {

    var expectedId = randomId();
    var httpServletRequest = MockMvcRequestBuilders
        .get("/test")
        .header("X-Request-ID", expectedId)
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();
    var loggedObject = getLoggedObject(consoleOut);

    assertThat(loggedObject).containsKey("id");
    assertThat(loggedObject.get("id")).isEqualTo(expectedId);
  }

  @Test
  void logErrorOnFilterChainException(CapturedOutput console) throws IOException, ServletException {

    var httpServletRequest = new MockHttpServletRequest();
    var httpServletResponse = new MockHttpServletResponse();

    doThrow(IOException.class).when(filterChain).doFilter(any(), any());
    assertThrows(IOException.class,
        () -> filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain));

    var consoleOut = console.getOut();

    assertThat(getLoggedObject(consoleOut)).containsKey("error");
  }

  @Test
  void doNotThrowWhenLogEntryCreationFails() {

    var httpServletRequest = MockMvcRequestBuilders
        .get("/test")
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    when(sensitiveDataMasker.maskHeaders(any())).thenThrow(RuntimeException.class);

    assertDoesNotThrow(
        () -> filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain));
  }

  @ParameterizedTest
  @ValueSource(strings = {"/", "/exclude"})
  void shouldNotLogWhenPathMatchExcludePattern(String excludePath, CapturedOutput console)
      throws IOException, ServletException {

    var httpServletRequest = MockMvcRequestBuilders
        .get(excludePath)
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilter(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();

    assertThat(getLoggedObject(consoleOut)).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"/actuator/health", "/docs/index.html"})
  void shouldNotLogWhenPathContainsExcludePattern(String excludePath, CapturedOutput console)
      throws IOException, ServletException {

    var httpServletRequest = MockMvcRequestBuilders
        .get(excludePath)
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilter(httpServletRequest, httpServletResponse, filterChain);

    var consoleOut = console.getOut();

    assertThat(getLoggedObject(consoleOut)).isEmpty();
  }

  private Map<String, Object> getLoggedObject(String consoleOut) throws JsonProcessingException {

    var startPos = consoleOut.indexOf("{");
    if (startPos > -1
        && consoleOut.substring(startPos, consoleOut.length() - 1).contains("}")) {
      var jsonString = consoleOut.substring(startPos);
      return (Map<String, Object>) objectMapper.readValue(jsonString, Map.class);
    }
    return Map.of();
  }

  private Map<String, Object> getLoggedObject(String consoleOut, int rowNumberToGet)
      throws JsonProcessingException {

    var startPos = consoleOut.indexOf("{");
    if (startPos > -1
        && consoleOut.substring(startPos, consoleOut.length() - 1).contains("}")) {
      var jsonString = consoleOut.substring(startPos);

      StringTokenizer tokenizer = new StringTokenizer(jsonString, "\n");
      int tokenCount = 0;
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        tokenCount++;
        int startCurlyBracketPos = token.indexOf("{");
        String subToken = token.substring(startCurlyBracketPos);

        if (tokenCount == rowNumberToGet) {
          return (Map<String, Object>) (objectMapper.readValue(subToken, Map.class));
        }
      }
    }
    return Map.of();
  }

  private Map<String, Object> getLoggedRequest(String consoleOut) throws JsonProcessingException {

    return (Map<String, Object>) getLoggedObject(consoleOut, 1).get("request");
  }

  private Map<String, Object> getLoggedRequestHeaders(String consoleOut)
      throws JsonProcessingException {

    var loggedRequest = getLoggedRequest(consoleOut);
    return (Map<String, Object>) loggedRequest.get("headers");
  }

  private Map<String, Object> getLoggedResponse(String consoleOut) throws JsonProcessingException {

    return (Map<String, Object>) getLoggedObject(consoleOut, 2).get("response");
  }

  private static String randomId() {
    return UUID.randomUUID().toString();
  }
}
