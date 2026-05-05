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
        "properties.logging-filter.enabled=false"
    })
public class LoggingFilterDisabledTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private LoggingFilter filter;

  @Mock
  private SensitiveDataMasker sensitiveDataMasker;

  private final MockFilterChain filterChain = new MockFilterChain();

  @Test
  void shouldNotLogWhenDisabled(CapturedOutput console) throws IOException, ServletException {

    var httpServletRequest = MockMvcRequestBuilders
        .get("/test")
        .buildRequest(new SpringBootMockServletContext("/"));
    var httpServletResponse = new MockHttpServletResponse();

    filter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

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
}
