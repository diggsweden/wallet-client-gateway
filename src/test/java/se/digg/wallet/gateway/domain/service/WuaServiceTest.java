// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.digg.wallet.gateway.application.controller.ApiKeyAuthFilterTest;
import se.digg.wallet.gateway.application.model.CreateWuaDto;
import se.digg.wallet.gateway.application.model.WuaDto;
import se.digg.wallet.gateway.infrastructure.walletprovider.client.WalletProviderClient;

@ExtendWith(MockitoExtension.class)
class WuaServiceTest {

  public static final UUID TEST_ATTRIBUTE_ID = UUID.randomUUID();

  @Mock
  private WalletProviderClient client;

  @InjectMocks
  private WuaService wuaService;

  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void createAttributeSuccess() {
    // Given
    CreateWuaDto createAttributeDto = ApiKeyAuthFilterTest.generateCreateWuaDto(TEST_ATTRIBUTE_ID);
    WuaDto expectedAttributeDto = new WuaDto("my dummy jwt");
    when(client.createWua(any()))
        .thenReturn("my dummy jwt");

    // When
    WuaDto actualAttributeDto = wuaService.createWua(createAttributeDto);

    // Then
    assertEquals(expectedAttributeDto, actualAttributeDto);
  }
}
