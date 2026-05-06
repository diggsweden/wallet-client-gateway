// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "properties.logging-filter.sensitive-data-mask.headers=sensitive,secret",
    "properties.logging-filter.sensitive-data-mask.body-json-fields=sensitive,secret",
    "properties.logging-filter.sensitive-data-mask.mask-value=***MASKED***"
})
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

    var emptyHeaders = new HashMap<String, String>();

    var maskedHeaders = masker.maskHeaders(emptyHeaders);

    assertThat(maskedHeaders).isEmpty();
  }

  @Test
  void nonSensitiveHeadersShouldReturnOriginValues() {

    var expectedHeaders = new HashMap<String, String>();
    expectedHeaders.put("FirstValue", randomString());
    expectedHeaders.put("AnotherValue", randomString());

    var actualHeaders = masker.maskHeaders(expectedHeaders);

    assertEquals(expectedHeaders.size(), actualHeaders.size());
    expectedHeaders.forEach((key, value) -> {
      assertTrue(actualHeaders.containsKey(key));
      assertEquals(value, actualHeaders.get(key));
    });
  }

  @Test
  void sensitiveHeaderShouldReturnMaskedValue() {

    var sensitiveHeader = "sensitive";
    var unmaskedValue = randomString();

    var unmaskedHeaders = new HashMap<String, String>();
    unmaskedHeaders.put("FirstHeader", randomString());
    unmaskedHeaders.put(sensitiveHeader, unmaskedValue);
    unmaskedHeaders.put("AnotherHeader", randomString());

    var maskedHeaders = masker.maskHeaders(unmaskedHeaders);

    assertEquals(unmaskedHeaders.size(), maskedHeaders.size());
    assertThat(maskedHeaders).containsKey(sensitiveHeader);
    var maskedValue = maskedHeaders.get(sensitiveHeader);
    assertThat(unmaskedValue).isNotEqualTo(maskedValue);
    assertThat(maskedValue).contains(MASKED);
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
  void nonSensitiveBodyShouldReturnOriginBody() throws JsonProcessingException {

    var unmaskedAttributes = new HashMap<String, Object>();
    unmaskedAttributes.put("FirstValue", randomString());
    unmaskedAttributes.put("AnotherValue", randomString());
    var expectedJsonBody = objectMapper.writeValueAsString(unmaskedAttributes);

    var actualOriginJsonBody = masker.maskJsonBody(expectedJsonBody);

    assertEquals(expectedJsonBody, actualOriginJsonBody);
  }

  @Test
  void sensitiveFieldValueInBodyShouldBeReturnedAsMasked() throws JsonProcessingException {

    var sensitiveAttribute = "sensitive";
    var unmaskedValue = "this is a sensitive value expected to be masked";

    var unmaskedAttributes = new HashMap<String, Object>();
    unmaskedAttributes.put("FirstValue", randomString());
    unmaskedAttributes.put(sensitiveAttribute, unmaskedValue);
    unmaskedAttributes.put("AnotherValue", randomString());
    var unmaskedJsonBody = objectMapper.writeValueAsString(unmaskedAttributes);

    var actualJsonBody = masker.maskJsonBody(unmaskedJsonBody);

    var actualBody = objectMapper.readValue(actualJsonBody, Map.class);
    assertThat(actualBody).containsKey(sensitiveAttribute);
    var maskedValue = actualBody.get(sensitiveAttribute).toString();
    assertThat(unmaskedValue).isNotEqualTo(maskedValue);
    assertThat(maskedValue).contains(MASKED);
  }

  @Test
  void emailAddressFieldValueInBodyShouldBeReturnedAsMasked() throws JsonProcessingException {

    var emailAttribute = "email";
    var expectedEmailDomain = "@test.domain.xx";
    var unmaskedEmail = String.format("test.testsson%s", expectedEmailDomain);

    var unmaskedAttributes = new HashMap<String, Object>();
    unmaskedAttributes.put("FirstValue", randomString());
    unmaskedAttributes.put(emailAttribute, unmaskedEmail);
    unmaskedAttributes.put("AnotherValue", randomString());
    var unmaskedJsonBody = objectMapper.writeValueAsString(unmaskedAttributes);

    var actualJsonBody = masker.maskJsonBody(unmaskedJsonBody);

    var actualBody = objectMapper.readValue(actualJsonBody, Map.class);
    assertThat(actualBody).containsKey(emailAttribute);
    var maskedValue = actualBody.get(emailAttribute).toString();
    assertThat(unmaskedEmail).isNotEqualTo(maskedValue);
    assertThat(maskedValue).contains(MASKED);
    assertThat(maskedValue).contains(expectedEmailDomain);
  }

  private static String randomString() {
    int length = 20;
    int leftLimit = 48; // numeral '0'
    int rightLimit = 122; // letter 'z'
    Random random = new Random();

    return random.ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }
}
