// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.digg.wallet.gateway.application.config.ApiKeyAuthFilter;
import se.digg.wallet.gateway.application.model.CreateAttributeDto;
import se.digg.wallet.gateway.domain.service.AttributeService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ApiKeyAuthFilterTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AttributeService attributeService;

  @Value("${properties.apiSecret}")
  private String apiKey;

  @Test
  void testValidApiKey() throws Exception {
    CreateAttributeDto createAttributeDto = new CreateAttributeDto("test");

    mockMvc
        .perform(
            post("/attributes")
                .header(ApiKeyAuthFilter.API_KEY_HEADER, apiKey)
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
