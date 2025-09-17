// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.digg.wallet.gateway.application.config.ApiKeyAuthFilter;
import se.digg.wallet.gateway.application.model.CreateAttributeDto;
import se.digg.wallet.gateway.domain.service.AttributeService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ApiKeyAuthFilterTest {

  public static final String SECRET_TEST_VALUE = "my-super-secret-test-value";
  public static final String TEST_SERVICE_URL = "http://test-service:8888";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AttributeService attributeService;

  /**
   * This static method is called before the application context is created. It adds properties that
   * will be used to resolve the placeholders in the YAML file. This acts like setting an
   * environment variable
   */
  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("API_SECRET", () -> SECRET_TEST_VALUE);
    registry.add("DOWNSTREAM_SERVICE_URL", () -> TEST_SERVICE_URL);
  }

  @Test
  void testValidApiKey() throws Exception {
    CreateAttributeDto createAttributeDto = new CreateAttributeDto("test");

    mockMvc
        .perform(
            post("/attributes")
                .header(ApiKeyAuthFilter.API_KEY_HEADER, SECRET_TEST_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAttributeDto)))
        .andExpect(status().isCreated());
  }

  @Test
  void testInvalidApiKey() throws Exception {
    CreateAttributeDto createAttributeDto = new CreateAttributeDto("test");

    mockMvc
        .perform(
            post("/attributes")
                .header(ApiKeyAuthFilter.API_KEY_HEADER, "invalid-api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAttributeDto)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testNoApiKey() throws Exception {
    CreateAttributeDto createAttributeDto = new CreateAttributeDto("test");

    mockMvc
        .perform(
            post("/attributes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAttributeDto)))
        .andExpect(status().isUnauthorized());
  }
}
