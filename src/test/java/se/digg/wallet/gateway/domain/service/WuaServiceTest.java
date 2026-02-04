// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.digg.wallet.gateway.application.model.CreateWuaDtoTestBuilder;
import se.digg.wallet.gateway.application.model.wua.WuaDto;
import se.digg.wallet.gateway.domain.service.wua.WuaMapper;
import se.digg.wallet.gateway.domain.service.wua.WuaService;
import se.digg.wallet.gateway.infrastructure.account.client.WalletAccountClient;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountAccountDto;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountJwkDto;
import se.digg.wallet.gateway.infrastructure.walletprovider.client.WalletProviderClient;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDto;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDtoV1;

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


  @Deprecated(forRemoval = true)
  @Test
  void createAttributeSuccess() {
    // Given
    var createWoaDto = CreateWuaDtoTestBuilder.withWalletId(TEST_ATTRIBUTE_ID);
    var expectedWua = new WuaDto("my dummy jwt");
    var mappedDto = new WalletProviderCreateWuaDtoV1("data", "doesnt matter");
    when(wuaMapper.toWalletProviderCreateWuaDto(createWoaDto))
        .thenReturn(mappedDto);
    when(client.createWua(mappedDto))
        .thenReturn(expectedWua.jwt());

    // When
    var actualWuaDto = wuaService.createWua(createWoaDto);

    // Then
    assertEquals(expectedWua, actualWuaDto);
    verify(client).createWua(any(WalletProviderCreateWuaDtoV1.class));
    verifyNoMoreInteractions(client);
  }

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
