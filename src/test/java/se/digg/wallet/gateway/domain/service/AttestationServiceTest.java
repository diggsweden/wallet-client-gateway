// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.digg.wallet.gateway.domain.common.JwkTestBuilder;
import se.digg.wallet.gateway.domain.model.common.Jwk;
import se.digg.wallet.gateway.domain.model.attestation.KeyAttestationBuilder;
import se.digg.wallet.gateway.domain.model.attestation.Wua;
import se.digg.wallet.gateway.domain.ports.outbound.AccountPort;
import se.digg.wallet.gateway.domain.ports.outbound.WalletProviderPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttestationServiceTest {

  private static final UUID ACCOUNT_ID = UUID.fromString("61128b3c-ef55-4410-8dff-d8e8bf0cb9a7");

  @Mock
  private WalletProviderPort walletProviderPort;

  @Mock
  private AccountPort accountPort;

  @InjectMocks
  private AttestationService attestationService;

  @Test
  void serves_wallet_unit_attestation() {
    // Given
    var nonce = "nonce";
    var walletKey = new Jwk("kty", "kid", "alg", "use", "crv", "x", "y");
    var expectedWua = new Wua("my dummy jwt");

    when(accountPort.getWalletKey(ACCOUNT_ID.toString())).thenReturn(walletKey);
    when(walletProviderPort.createWalletUnitAttestation(eq(walletKey), eq(nonce)))
        .thenReturn(expectedWua);

    // When
    var actualWuaDto = attestationService.createWua(ACCOUNT_ID.toString(), nonce);

    // Then
    assertEquals(expectedWua, actualWuaDto);
    verify(accountPort).getWalletKey(any(String.class));
    verifyNoMoreInteractions(accountPort);
    verify(walletProviderPort).createWalletUnitAttestation(any(Jwk.class), any(String.class));
    verifyNoMoreInteractions(walletProviderPort);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"the-nonce-value"})
  void serves_key_attestation(String nonce) {

    var expectedResultJwt = "123456";
    var keyAttestation = KeyAttestationBuilder.builder()
        .jwt(expectedResultJwt)
        .build();
    when(walletProviderPort.createKeyAttestation(any(), any())).thenReturn(keyAttestation);
    var keys = List.of(JwkTestBuilder.withDefaults().build());

    var result = attestationService.createKeyAttestation(keys, nonce);

    assertThat(result).isNotNull();
    assertThat(result.jwt()).isEqualTo(expectedResultJwt);
  }
}
