// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class LoggingFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);
  public static final String MDC_TRANSACTION_ID = "transactionId";
  public static final String MDC_CORRELATION_ID = "id";

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private SensitiveDataMasker sensitiveDataMasker;

  @Value("${properties.logging-filter.enabled:false}")
  private boolean isLoggingEnabled;

  @Value("${properties.logging-filter.max-payload-length:10000}")
  private int maxPayloadLength;

  @Value("${properties.logging-filter.exclude-path.exact-match:}")
  private List<String> excludePathExactMatch;

  @Value("${properties.logging-filter.exclude-path.contains:}")
  private List<String> excludePathContains;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    // Generate Trace IDs
    String correlationId = getOrGenerateCorrelationId(request);
    String transactionId = UUID.randomUUID().toString();

    // Add to Mapped Diagnostic Context - MDC for log correlation
    MDC.put(MDC_CORRELATION_ID, correlationId);
    MDC.put(MDC_TRANSACTION_ID, transactionId);

    // Wrap request and response
    ContentCachingRequestWrapper wrappedRequest =
        new ContentCachingRequestWrapper(request, maxPayloadLength);
    ContentCachingResponseWrapper wrappedResponse =
        new ContentCachingResponseWrapper(response);

    // Add correlation ID to response header
    wrappedResponse.setHeader("X-Correlation-ID", correlationId);

    Instant startTime = Instant.now();
    Exception capturedException = null;

    try {
      filterChain.doFilter(wrappedRequest, wrappedResponse);
    } catch (Exception e) {
      capturedException = e;
      throw e;
    } finally {
      // Calculate duration
      long durationMs = Instant.now().toEpochMilli() - startTime.toEpochMilli();

      if (isLoggingEnabled) {
        // Build structured log entry
        logStructuredEntry(
            correlationId,
            wrappedRequest,
            wrappedResponse,
            durationMs,
            capturedException);
      }

      // Copy response body back
      wrappedResponse.copyBodyToResponse();

      // Clean up MDC
      MDC.clear();
    }
  }

  /**
   * Gets existing correlation ID from request header or generates a new one.
   */
  private String getOrGenerateCorrelationId(HttpServletRequest request) {
    String correlationId = request.getHeader("X-Correlation-ID");
    if (correlationId == null || correlationId.isEmpty()) {
      correlationId = request.getHeader("X-Request-ID");
    }
    if (correlationId == null || correlationId.isEmpty()) {
      correlationId = UUID.randomUUID().toString();
    }
    return correlationId;
  }

  /**
   * Builds and logs a structured log entry.
   */
  private void logStructuredEntry(
      String correlationId,
      ContentCachingRequestWrapper request,
      ContentCachingResponseWrapper response,
      long durationMs,
      Exception exception) {

    try {
      String timestamp = Instant.now().toString();

      doLog(timestamp, correlationId, "request", requestDetails(request), durationMs, exception,
          response.getStatus());
      doLog(timestamp, correlationId, "response", responseDetails(response), durationMs, exception,
          response.getStatus());

    } catch (Throwable e) {
      LOGGER.error("Failed to create structured log entry", e);
    }
  }

  private void doLog(String timestamp, String correlationId, String key, Object details,
      long durationMs, Exception exception, int responseStatus)
      throws JsonProcessingException {
    Map<String, Object> logEntry = new LinkedHashMap<>();

    // Basic request info
    logEntry.put("timestamp", timestamp);
    logEntry.put("id", correlationId);
    logEntry.put("type", "http_request");

    // request or response details
    logEntry.put(key, details);

    // Timing
    logEntry.put("durationMs", durationMs);

    // Error info if present
    if (exception != null) {
      logEntry.put("error", errorDetails(exception));
    }

    writeAsJsonToLogger(logEntry, responseStatus);
  }

  private Map<String, Object> requestDetails(ContentCachingRequestWrapper request) {
    // Request details
    Map<String, Object> requestDetails = new LinkedHashMap<>();
    requestDetails.put("method", request.getMethod());
    requestDetails.put("path", request.getRequestURI());
    requestDetails.put("queryString", request.getQueryString());
    requestDetails.put("remoteAddress", request.getRemoteAddr());
    requestDetails.put("userAgent", request.getHeader("User-Agent"));
    requestDetails.put("contentType", request.getContentType());
    requestDetails.put("contentLength", request.getContentLength());

    // Masked headers
    requestDetails.put("headers", sensitiveDataMasker.maskHeaders(extractHeaders(request)));

    // Masked request body
    String requestBody = getPayload(request.getContentAsByteArray());
    if (!requestBody.isEmpty()) {
      requestDetails.put("body", sensitiveDataMasker.maskJsonBody(requestBody));
    }

    return requestDetails;
  }

  private Map<String, Object> responseDetails(ContentCachingResponseWrapper response) {
    // Response details
    Map<String, Object> responseDetails = new LinkedHashMap<>();
    responseDetails.put("status", response.getStatus());
    responseDetails.put("contentType", response.getContentType());

    // Response body
    String responseBody = getPayload(response.getContentAsByteArray());
    if (!responseBody.isEmpty()) {
      responseDetails.put("body", sensitiveDataMasker.maskJsonBody(responseBody));
    }

    return responseDetails;
  }

  private Map<String, Object> errorDetails(Exception exception) {
    Map<String, Object> errorDetails = new LinkedHashMap<>();
    errorDetails.put("type", exception.getClass().getName());
    errorDetails.put("message", exception.getMessage());

    return errorDetails;
  }

  private void writeAsJsonToLogger(Map<String, Object> logEntry, int status)
      throws JsonProcessingException {
    // Log as JSON
    String jsonLog = objectMapper.writeValueAsString(logEntry);

    if (status >= 500) {
      LOGGER.error(jsonLog);
    } else if (status >= 400) {
      LOGGER.warn(jsonLog);
    } else {
      LOGGER.info(jsonLog);
    }
  }

  /**
   * Extracts headers from request into a map.
   */
  private Map<String, String> extractHeaders(HttpServletRequest request) {

    Map<String, String> headers = new HashMap<>();
    Collections.list(request.getHeaderNames())
        .forEach(name -> headers.put(name, request.getHeader(name)));

    return headers;
  }

  /**
   * Converts byte array to string with truncation.
   */
  private String getPayload(byte[] content) {

    if (content == null || content.length == 0) {
      return "";
    }

    int length = Math.min(content.length, maxPayloadLength);
    String payload = new String(content, 0, length, StandardCharsets.UTF_8);
    if (content.length > maxPayloadLength) {
      payload += "...[truncated]";
    }

    return payload;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {

    String path = request.getRequestURI();

    return excludePathExactMatch.stream().anyMatch(path::equals)
        || excludePathContains.stream().anyMatch(path::contains);
  }
}
