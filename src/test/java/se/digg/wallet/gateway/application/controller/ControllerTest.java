// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.digg.wallet.gateway.application.model.CreateWuaDto;
import se.digg.wallet.gateway.application.model.CreateWuaDtoTestBuilder;
import se.digg.wallet.gateway.application.model.WuaDto;
import se.digg.wallet.gateway.domain.service.WuaService;

@WebMvcTest(Controller.class)
class ControllerTest {

  public static final String TEST_ATTRIBUTE_VALUE = "test attribute value";
  public static final UUID TEST_ATTRIBUTE_ID = UUID.randomUUID();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private WuaService wuaService;

  @Test
  @WithMockUser
  void testCreateAttributeHappyPath() throws Exception {
    CreateWuaDto createWuaDto = CreateWuaDtoTestBuilder.withWalletId(TEST_ATTRIBUTE_ID);

    when(wuaService.createWua(any(CreateWuaDto.class)))
        .thenReturn(new WuaDto(TEST_ATTRIBUTE_VALUE));

    mockMvc
        .perform(
            post("/wua")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createWuaDto))
                .with(csrf()))
        .andExpect(status().isCreated());
  }
}
