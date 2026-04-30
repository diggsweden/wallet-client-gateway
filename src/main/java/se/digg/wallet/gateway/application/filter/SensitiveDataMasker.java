// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility for masking sensitive data in logs.
 * Prevents accidental exposure of passwords, tokens and personal data.
 */
@Component
public class SensitiveDataMasker {

  private final ObjectMapper objectMapper;

  @Value("${properties.logging-filter.sensitive-data-mask.headers:}")
  private List<String> sensitiveHeaders;

  @Value("${properties.logging-filter.sensitive-data-mask.fields:}")
  private List<String> sensitiveFields;

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

  private static final String MASK = "***MASKED***";

  public SensitiveDataMasker(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Masks sensitive headers in a map.
   */
  public Map<String, String> maskHeaders(Map<String, String> headers) {
    Map<String, String> maskedHeaders = new HashMap<>();

    headers.forEach((key, value) -> {
      if (sensitiveHeaders.contains(key.toLowerCase())) {
        // Mask the value but show first few characters
        maskedHeaders.put(key, maskValue(value));
      } else {
        maskedHeaders.put(key, value);
      }
    });

    return maskedHeaders;
  }

  /**
   * Masks sensitive fields in a JSON body.
   */
  public String maskJsonBody(String jsonBody) {
    if (jsonBody == null || jsonBody.isEmpty()) {
      return jsonBody;
    }

    try {
      JsonNode rootNode = objectMapper.readTree(jsonBody);
      maskJsonNode(rootNode);
      return objectMapper.writeValueAsString(rootNode);
    } catch (JsonProcessingException e) {
      // Not valid JSON, apply pattern-based masking
      return maskPatterns(jsonBody);
    }
  }

  /**
   * Recursively masks sensitive fields in a JSON node.
   */
  private void maskJsonNode(JsonNode node) {
    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      objectNode.fieldNames().forEachRemaining(fieldName -> {
        JsonNode childNode = objectNode.get(fieldName);

        if (sensitiveFields.contains(fieldName.toLowerCase())) {
          // Mask the entire value
          objectNode.put(fieldName, MASK);
        } else if (childNode.isObject() || childNode.isArray()) {
          // Recurse into nested structures
          maskJsonNode(childNode);
        } else if (childNode.isTextual()) {
          // Check for patterns in string values
          String value = childNode.asText();
          String maskedValue = maskPatterns(value);
          if (!value.equals(maskedValue)) {
            objectNode.put(fieldName, maskedValue);
          }
        }
      });
    } else if (node.isArray()) {
      node.forEach(this::maskJsonNode);
    }
  }

  /**
   * Applies pattern-based masking to a string.
   */
  private String maskPatterns(String value) {
    // Mask email addresses
    return EMAIL_PATTERN.matcher(value).replaceAll(match -> {
      String email = match.group();
      int atIndex = email.indexOf('@');
      if (atIndex > 2) {
        return email.substring(0, 2) + "***@" + email.substring(atIndex + 1);
      }
      return "***@" + email.substring(atIndex + 1);
    });
  }

  /**
   * Masks a value showing only first few characters.
   */
  private String maskValue(String value) {
    if (value == null || value.length() <= 4) {
      return MASK;
    }
    return value.substring(0, 4) + "..." + MASK;
  }
}
