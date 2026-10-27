// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Masks headers and bodies before they are logged. Only explicitly allowed header and JSON field
 * names are logged in clear; everything else is masked, so new fields are never logged by accident.
 */
@Component
public class SensitiveDataMasker {

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${properties.logging-filter.sensitive-data-mask.allowed-headers:}")
  private List<String> allowedHeaders;

  @Value("${properties.logging-filter.sensitive-data-mask.allowed-body-json-fields:}")
  private List<String> allowedFields;

  @Value("${properties.logging-filter.sensitive-data-mask.mask-value:***MASKED***}")
  private String mask;

  private Set<String> normalizedAllowedHeaders;

  private Set<String> normalizedAllowedFields;

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

  @PostConstruct
  void normalizeConfiguration() {
    // Lookups are made with lower case keys, so configured names must be lower case as well
    normalizedAllowedHeaders = toLowerCase(allowedHeaders);
    normalizedAllowedFields = toLowerCase(allowedFields);
  }

  private static Set<String> toLowerCase(List<String> values) {
    return values.stream()
        .map(value -> value.trim().toLowerCase(Locale.ROOT))
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Masks the value of every header that is not allowed.
   */
  public Map<String, String> maskHeaders(Map<String, String> headers) {

    if (headers == null) {
      return null;
    }

    Map<String, String> maskedHeaders = new HashMap<>();

    headers.forEach((key, value) -> maskedHeaders.put(key,
        normalizedAllowedHeaders.contains(key.toLowerCase(Locale.ROOT)) ? value : mask));

    return maskedHeaders;
  }

  /**
   * Masks every value in a JSON body whose field name is not allowed. A body that is not a JSON
   * object or array is masked entirely.
   */
  public String maskJsonBody(String jsonBody) {
    if (jsonBody == null || jsonBody.isEmpty()) {
      return jsonBody;
    }

    try {
      JsonNode rootNode = objectMapper.readTree(jsonBody);
      if (rootNode == null || !rootNode.isContainerNode()) {
        return mask;
      }
      maskJsonNode(rootNode);
      return objectMapper.writeValueAsString(rootNode);
    } catch (JsonProcessingException e) {
      return mask;
    }
  }

  /*
   * Containers are always traversed so their allowed fields stay readable. A scalar value is only
   * kept when its field name is allowed; values in arrays have no name of their own and are masked.
   */
  private void maskJsonNode(JsonNode node) {
    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      objectNode.fieldNames().forEachRemaining(fieldName -> {
        JsonNode childNode = objectNode.get(fieldName);

        if (childNode.isContainerNode()) {
          maskJsonNode(childNode);
        } else if (normalizedAllowedFields.contains(fieldName.toLowerCase(Locale.ROOT))) {
          // Allowed values are logged in clear, but e-mail addresses inside them are still masked
          if (childNode.isTextual()) {
            objectNode.put(fieldName, maskPatterns(childNode.asText()));
          }
        } else if (!childNode.isNull()) {
          objectNode.put(fieldName, mask);
        }
      });
    } else if (node.isArray()) {
      ArrayNode arrayNode = (ArrayNode) node;
      for (int i = 0; i < arrayNode.size(); i++) {
        JsonNode childNode = arrayNode.get(i);
        if (childNode.isContainerNode()) {
          maskJsonNode(childNode);
        } else if (!childNode.isNull()) {
          arrayNode.set(i, mask);
        }
      }
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
        return String.format("%s%s@%s",
            email.substring(0, 2), mask, email.substring(atIndex + 1));
      }
      return String.format("%s@%s", mask, email.substring(atIndex + 1));
    });
  }
}
