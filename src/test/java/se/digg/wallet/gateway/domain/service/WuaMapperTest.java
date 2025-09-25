// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.digg.wallet.gateway.application.model.CreateWuaDtoTestBuilder;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDto;

@ExtendWith(MockitoExtension.class)
class WuaMapperTest {

  public static final UUID TEST_ATTRIBUTE_ID = UUID.randomUUID();

  @Spy
  private ObjectMapper objectMapper;

  @InjectMocks
  private WuaMapper wuaMapper;


  @Test
  void map() throws Exception {
    // Given
    var createAttributeDto = CreateWuaDtoTestBuilder.withWalletId(TEST_ATTRIBUTE_ID);
    var expectedAttributeDto =
        new WalletProviderCreateWuaDto(createAttributeDto.walletId().toString(),
            objectMapper.writeValueAsString(createAttributeDto.jwk()));

    // When
    var actualAttributeDto = wuaMapper.toWalletProviderCreateWuaDto(createAttributeDto);

    // Then
    assertEquals(expectedAttributeDto, actualAttributeDto);
  }
}
