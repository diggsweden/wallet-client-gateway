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
import se.digg.wallet.gateway.application.auth.ApiKeyVerifier;
import se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder;
import se.digg.wallet.gateway.application.model.JwkDtoTestBuilder;
import se.digg.wallet.gateway.application.model.account.CreateAccountResponseDto;
import se.digg.wallet.gateway.domain.service.account.AccountService;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

  public static final UUID TEST_ACCOUNT_ID = UUID.randomUUID();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AccountService accountService;

  @MockitoBean
  private ApiKeyVerifier apiKeyVerifier;

  @Test
  @WithMockUser
  void testCreateAccountHappyPath() throws Exception {
    var requestBody = CreateAccountRequestDtoTestBuilder.withDefaults().build();

    when(accountService.createAccount(any()))
        .thenReturn(new CreateAccountResponseDto(TEST_ACCOUNT_ID));

    mockMvc
        .perform(
            post("/accounts/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .with(csrf()))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser
  void testCreateAccountWithoutKid() throws Exception {
    var requestBody = CreateAccountRequestDtoTestBuilder.withDefaults()
        .publicKey(JwkDtoTestBuilder.withDefaults().kid("").build())
        .build();

    when(accountService.createAccount(any()))
        .thenReturn(new CreateAccountResponseDto(TEST_ACCOUNT_ID));

    mockMvc
        .perform(
            post("/accounts/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .with(csrf()))
        .andExpect(status().isBadRequest());
  }
}
