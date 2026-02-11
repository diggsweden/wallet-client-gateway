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
import se.digg.wallet.gateway.domain.service.wua.WuaMapper;
import se.digg.wallet.gateway.domain.service.wua.WuaService;
import se.digg.wallet.gateway.infrastructure.account.client.WalletAccountClient;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountAccountDto;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountJwkDto;
import se.digg.wallet.gateway.infrastructure.walletprovider.client.WalletProviderClient;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDto;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WuaServiceTest {

  public static final UUID TEST_ATTRIBUTE_ID = UUID.randomUUID();

  @Mock
  private WalletProviderClient client;

  @Mock
  private WuaMapper wuaMapper;

  @Mock
  private WalletAccountClient accountClient;

  @InjectMocks
  private WuaService wuaService;

  @Test
  void createWua() {
    // Given
    var accountId = UUID.randomUUID();
    var nonce = "nonce";
    var publicKey = new WalletAccountJwkDto("kty", "kid", "alg", "use", "crv", "x", "y");
    var accountDto = new WalletAccountAccountDto(accountId, "", "",
        Optional.empty(), publicKey);
    var expectedWua = new WuaDto("my dummy jwt");
    var mappedDto = new WalletProviderCreateWuaDto("data", "nonce");

    when(accountClient.getAccount(accountId.toString()))
        .thenReturn(Optional.of(accountDto));
    when(wuaMapper.toWalletProviderCreateWuaDto(accountDto, nonce))
        .thenReturn(mappedDto);
    when(client.createWua(mappedDto))
        .thenReturn(expectedWua.jwt());

    // When
    var actualWuaDto = wuaService.createWua(accountId.toString(), nonce);

    // Then
    assertEquals(expectedWua, actualWuaDto);
    verify(accountClient).getAccount(any(String.class));
    verifyNoMoreInteractions(accountClient);
    verify(wuaMapper).toWalletProviderCreateWuaDto(any(WalletAccountAccountDto.class),
        any(String.class));
    verifyNoMoreInteractions(wuaMapper);
    verify(client).createWua(any(WalletProviderCreateWuaDto.class));
    verifyNoMoreInteractions(client);
  }
}
