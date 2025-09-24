// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import se.digg.wallet.gateway.application.model.CreateWuaDto;
import se.digg.wallet.gateway.application.model.WuaDto;
import se.digg.wallet.gateway.infrastructure.downstream.client.WalletProviderClient;
import se.digg.wallet.gateway.infrastructure.downstream.model.WalletProviderWuaDto;

class WuaServiceTest {

  public static final String TEST_ATTRIBUTE_VALUE = "test attribute value";
  public static final String TEST_ATTRIBUTE_ID = "12345";

  @Mock
  private WalletProviderClient client;

  private WuaService wuaService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    wuaService = new WuaService(client);
  }

  @Test
  void createAttributeSuccess() {
    // Given
    CreateWuaDto createAttributeDto = new CreateWuaDto(TEST_ATTRIBUTE_VALUE);
    WuaDto expectedAttributeDto = new WuaDto(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE);
    when(client.createAttribute(any()))
        .thenReturn(new WalletProviderWuaDto(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE));

    // When
    WuaDto actualAttributeDto = wuaService.createWua(createAttributeDto);

    // Then
    assertEquals(expectedAttributeDto, actualAttributeDto);
  }

  @Test
  void getAttributeSuccess() {
    // Given
    WuaDto expectedAttributeDto = new WuaDto(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE);

    when(client.getWua(TEST_ATTRIBUTE_ID))
        .thenReturn(new WalletProviderWuaDto(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE));
    // When
    WuaDto actualAttributeDto = wuaService.getWua(TEST_ATTRIBUTE_ID);

    // Then
    assertEquals(expectedAttributeDto, actualAttributeDto);
  }

  @Test
  void getAttributeNotFound() {
    when(client.getWua(anyString()))
        .thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404)));

    // When & Then
    assertThrows(
        HttpClientErrorException.class, () -> wuaService.getWua(TEST_ATTRIBUTE_ID));
  }
}
