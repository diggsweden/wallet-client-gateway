// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.digg.wallet.gateway.application.model.wua.WuaDto;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.ports.outbound.AccountPort;
import se.digg.wallet.gateway.domain.service.wua.WuaMapper;
import se.digg.wallet.gateway.domain.service.wua.WuaService;
import se.digg.wallet.gateway.infrastructure.walletprovider.client.WalletProviderClient;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDto;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WuaServiceTest {

  @Mock
  private WalletProviderClient client;

  @Mock
  private WuaMapper wuaMapper;

  @Mock
  private AccountPort accountPort;

  @InjectMocks
  private WuaService wuaService;

  @Test
  void createWua() {
    // Given
    var accountId = UUID.randomUUID();
    var nonce = "nonce";
    var walletKey = new Jwk("kty", "kid", "alg", "use", "crv", "x", "y");
    var expectedWua = new WuaDto("my dummy jwt");
    var mappedDto = new WalletProviderCreateWuaDto("data", "nonce");

    when(accountPort.getWalletKey(accountId.toString())).thenReturn(walletKey);
    when(wuaMapper.toWalletProviderCreateWuaDto(walletKey, nonce)).thenReturn(mappedDto);
    when(client.createWua(mappedDto)).thenReturn(expectedWua.jwt());

    // When
    var actualWuaDto = wuaService.createWua(accountId.toString(), nonce);

    // Then
    assertEquals(expectedWua, actualWuaDto);
    verify(accountPort).getWalletKey(any(String.class));
    verifyNoMoreInteractions(accountPort);
    verify(wuaMapper).toWalletProviderCreateWuaDto(any(Jwk.class), any(String.class));
    verifyNoMoreInteractions(wuaMapper);
    verify(client).createWua(any(WalletProviderCreateWuaDto.class));
    verifyNoMoreInteractions(client);
  }
}
