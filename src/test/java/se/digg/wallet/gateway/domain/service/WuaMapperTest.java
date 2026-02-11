// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.digg.wallet.gateway.domain.service.wua.WuaMapper;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountAccountDto;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountJwkDto;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDto;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class WuaMapperTest {

  public static final UUID TEST_ATTRIBUTE_ID = UUID.randomUUID();
  private static final String TEST_NONCE = "nonce";

  @Spy
  private ObjectMapper objectMapper;

  @InjectMocks
  private WuaMapper wuaMapper;


  @Test
  void mapFromAccountAndNonce() throws Exception {
    // Given
    var publicKey = new WalletAccountJwkDto("kty", "kid", "alg", "use", "crv", "x", "y");
    var accountDto = new WalletAccountAccountDto(UUID.randomUUID(), "", "",
        Optional.empty(), publicKey);
    var expectedWuaDto =
        new WalletProviderCreateWuaDto(objectMapper.writeValueAsString(accountDto.publicKey()),
            TEST_NONCE);

    // When
    var actualWuaDto = wuaMapper.toWalletProviderCreateWuaDto(accountDto, TEST_NONCE);

    // Then
    assertEquals(expectedWuaDto, actualWuaDto);
  }
}
