// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.service.wua.WuaMapper;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDto;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class WuaMapperTest {

  private static final String TEST_NONCE = "nonce";

  @Spy
  private ObjectMapper objectMapper;

  @InjectMocks
  private WuaMapper wuaMapper;

  @Test
  void mapFromWalletKeyAndNonce() throws Exception {
    // Given
    var walletKey = new Jwk("kty", "kid", "alg", "use", "crv", "x", "y");
    var expectedWuaDto =
        new WalletProviderCreateWuaDto(objectMapper.writeValueAsString(walletKey), TEST_NONCE);

    // When
    var actualWuaDto = wuaMapper.toWalletProviderCreateWuaDto(walletKey, TEST_NONCE);

    // Then
    assertEquals(expectedWuaDto, actualWuaDto);
  }
}
