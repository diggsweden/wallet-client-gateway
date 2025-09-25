// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.digg.wallet.gateway.application.config.ApiKeyAuthFilter;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.model.CreateWuaDto;
import se.digg.wallet.gateway.application.model.JwkDto;
import se.digg.wallet.gateway.domain.service.WuaService;

@SpringBootTest()
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ApiKeyAuthFilterTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private WuaService wuaService;

  @Autowired
  ApplicationConfig applicationConfig;

  @Test
  void testValidApiKey() throws Exception {
    CreateWuaDto createWuaDto = generateCreateWuaDto(UUID.randomUUID());

    mockMvc
        .perform(
            post("/wua")
                .header(ApiKeyAuthFilter.API_KEY_HEADER, applicationConfig.apisecret())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createWuaDto)))
        .andExpect(status().isCreated());
  }

  @Test
  void testInvalidApiKey() throws Exception {
    CreateWuaDto createWuaDto = generateCreateWuaDto(UUID.randomUUID());

    mockMvc
        .perform(
            post("/wua")
                .header(ApiKeyAuthFilter.API_KEY_HEADER, "invalid-api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createWuaDto)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testNoApiKey() throws Exception {
    CreateWuaDto createWuaDto = generateCreateWuaDto(UUID.randomUUID());

    mockMvc
        .perform(
            post("/wua")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createWuaDto)))
        .andExpect(status().isUnauthorized());
  }

  public static CreateWuaDto generateCreateWuaDto(UUID walletId) {
    return new CreateWuaDto(walletId, new JwkDto("kty", "kid", "alg", "use"));
  }
}
