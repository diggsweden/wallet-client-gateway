// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(properties = {
    "properties.logging-filter.sensitive-data-mask.allowed-headers=content-type,Mixed-Case-Header",
    "properties.logging-filter.sensitive-data-mask.allowed-body-json-fields=status,mixedCaseField,allowedObject",
    "properties.logging-filter.sensitive-data-mask.mask-value=***MASKED***"
})
@ActiveProfiles("test")
public class SensitiveDataMaskerTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private SensitiveDataMasker masker;

  private static final String MASKED = "***MASKED***";

  @Test
  void nullHeadersShouldReturnNull() {

    assertNull(masker.maskHeaders(null));
  }

  @Test
  void emptyHeadersShouldReturnEmpty() {

    assertThat(masker.maskHeaders(new HashMap<>())).isEmpty();
  }

  @Test
  void allowedHeadersShouldReturnOrigin() {

    var headers = Map.of(
        "Content-Type", "application/json",
        "mixed-case-header", "some value");

    assertThat(masker.maskHeaders(headers)).isEqualTo(headers);
  }

  @ParameterizedTest
  @ValueSource(strings = {"SESSION", "X-API-KEY", "Authorization", "Some-New-Header"})
  void headersNotAllowedShouldBeMasked(String header) {

    var maskedHeaders = masker.maskHeaders(Map.of(header, "3f2e7c0b-91d2-4a7e-9f34-0d8d9a5a9e77"));

    assertThat(maskedHeaders).containsEntry(header, MASKED);
  }

  @Test
  void nullHeaderValueNotAllowedShouldBeMasked() {

    var headers = new HashMap<String, String>();
    headers.put("SESSION", null);

    assertThat(masker.maskHeaders(headers)).containsEntry("SESSION", MASKED);
  }

  @Test
  void nullBodyShouldReturnNull() {

    assertNull(masker.maskJsonBody(null));
  }

  @Test
  void emptyBodyShouldReturnEmpty() {

    assertThat(masker.maskJsonBody("")).isEmpty();
  }

  @Test
  void allowedFieldsShouldReturnOriginValues() throws Exception {

    var masked = mask("""
        {"status": "COMPLETE", "MIXEDCASEFIELD": 42}
        """);

    assertThat(masked.get("status").asText()).isEqualTo("COMPLETE");
    assertThat(masked.get("MIXEDCASEFIELD").asInt()).isEqualTo(42);
  }

  @Test
  void fieldsNotAllowedShouldBeMasked() throws Exception {

    var masked = mask("""
        {
          "unknownField": "some value",
          "sessionId": "3f2e7c0b-91d2-4a7e-9f34-0d8d9a5a9e77",
          "pin": 1234,
          "verified": true
        }
        """);

    assertThat(masked.get("unknownField").asText()).isEqualTo(MASKED);
    assertThat(masked.get("sessionId").asText()).isEqualTo(MASKED);
    assertThat(masked.get("pin").asText()).isEqualTo(MASKED);
    assertThat(masked.get("verified").asText()).isEqualTo(MASKED);
  }

  @Test
  void nullValueShouldStayNull() throws Exception {

    var masked = mask("""
        {"jwt": null}
        """);

    assertThat(masked.get("jwt").isNull()).isTrue();
  }

  @Test
  void containersShouldBeTraversedAndOnlyAllowedFieldsKept() throws Exception {

    var masked = mask("""
        {
          "result": {"status": "PENDING", "jwt": "eyJhbGciOiJFUzI1NiJ9.e30.c2ln"},
          "items": [{"status": "ACTIVE", "secret": "s"}, "loose value", 7, null]
        }
        """);

    assertThat(masked.at("/result/status").asText()).isEqualTo("PENDING");
    assertThat(masked.at("/result/jwt").asText()).isEqualTo(MASKED);
    assertThat(masked.at("/items/0/status").asText()).isEqualTo("ACTIVE");
    assertThat(masked.at("/items/0/secret").asText()).isEqualTo(MASKED);
    assertThat(masked.at("/items/1").asText()).isEqualTo(MASKED);
    assertThat(masked.at("/items/2").asText()).isEqualTo(MASKED);
    assertThat(masked.at("/items/3").isNull()).isTrue();
  }

  @Test
  void allowedContainerShouldBeLoggedWithEverythingBelowIt() throws Exception {

    var masked = mask("""
        {"allowedObject": {"anyName": "value", "list": [1, 2]}}
        """);

    assertThat(masked.at("/allowedObject/anyName").asText()).isEqualTo("value");
    assertThat(masked.at("/allowedObject/list/1").asInt()).isEqualTo(2);
  }

  @Test
  void emailAddressInAllowedFieldShouldBeMasked() throws Exception {

    var masked = mask("""
        {"status": "sent to test.testsson@test.domain.xx"}
        """);

    assertThat(masked.get("status").asText())
        .doesNotContain("test.testsson")
        .contains(MASKED)
        .endsWith("@test.domain.xx");
  }

  @ParameterizedTest
  @ValueSource(strings = {"not json at all", "\"a json string\"", "1234", "  "})
  void bodyThatIsNotJsonObjectOrArrayShouldBeMaskedEntirely(String body) {

    assertThat(masker.maskJsonBody(body)).isEqualTo(MASKED);
  }

  private JsonNode mask(String body) throws Exception {
    return objectMapper.readTree(masker.maskJsonBody(body));
  }
}
