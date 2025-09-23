// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import se.digg.wallet.gateway.application.model.AttributeDto;
import se.digg.wallet.gateway.application.model.CreateAttributeDto;
import se.digg.wallet.gateway.domain.service.AttributeService;

@WebMvcTest(AttributeController.class)
class AttributeControllerTest {

  public static final String TEST_ATTRIBUTE_VALUE = "test attribute value";
  public static final String TEST_ATTRIBUTE_ID = "12345";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AttributeService attributeService;

  @Test
  @WithMockUser
  void testCreateAttributeHappyPath() throws Exception {
    CreateAttributeDto createAttributeDto = new CreateAttributeDto(TEST_ATTRIBUTE_VALUE);

    when(attributeService.createAttribute(any(CreateAttributeDto.class)))
        .thenReturn(new AttributeDto(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE));

    mockMvc
        .perform(
            post("/attributes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAttributeDto))
                .with(csrf()))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser
  void testGetAttributeNotFound() throws Exception {
    when(attributeService.getAttribute(TEST_ATTRIBUTE_ID))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    mockMvc.perform(get("/attributes/" + TEST_ATTRIBUTE_ID)).andExpect(status().is5xxServerError());
  }

}
