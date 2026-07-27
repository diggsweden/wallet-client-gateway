// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.wua.Wua;
import se.digg.wallet.gateway.domain.ports.outbound.AccountPort;
import se.digg.wallet.gateway.domain.ports.outbound.WalletProviderPort;
import se.digg.wallet.gateway.domain.service.wua.WuaService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WuaServiceTest {

  private static final UUID ACCOUNT_ID = UUID.fromString("61128b3c-ef55-4410-8dff-d8e8bf0cb9a7");

  @Mock
  private WalletProviderPort walletProviderAdapter;

  @Mock
  private AccountPort accountPort;

  @InjectMocks
  private WuaService wuaService;

  @Test
  void createWua() {
    // Given
    var nonce = "nonce";
    var walletKey = new Jwk("kty", "kid", "alg", "use", "crv", "x", "y");
    var expectedWua = new Wua("my dummy jwt");

    when(accountPort.getWalletKey(ACCOUNT_ID.toString())).thenReturn(walletKey);
    when(walletProviderAdapter.createWalletUnitAttestation(eq(walletKey), eq(nonce)))
        .thenReturn(expectedWua);

    // When
    var actualWuaDto = wuaService.createWua(ACCOUNT_ID.toString(), nonce);

    // Then
    assertEquals(expectedWua, actualWuaDto);
    verify(accountPort).getWalletKey(any(String.class));
    verifyNoMoreInteractions(accountPort);
    verify(walletProviderAdapter).createWalletUnitAttestation(any(Jwk.class), any(String.class));
    verifyNoMoreInteractions(walletProviderAdapter);
  }
}
