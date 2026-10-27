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
import se.digg.wallet.gateway.domain.model.attestation.WalletInstanceAttestationBuilder;
import se.digg.wallet.gateway.domain.model.attestation.KeyAttestationBuilder;
import se.digg.wallet.gateway.domain.ports.outbound.AccountPort;
import se.digg.wallet.gateway.domain.ports.outbound.AttestationIssuerPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttestationServiceTest {

  private static final UUID ACCOUNT_ID = UUID.fromString("61128b3c-ef55-4410-8dff-d8e8bf0cb9a7");
  private static final String JWT = "123456";

  @Mock
  private AttestationIssuerPort attestationIssuerPort;

  @Mock
  private AccountPort accountPort;

  @InjectMocks
  private AttestationService attestationService;

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"the-nonce-value"})
  void serves_key_attestation(String nonce) {

    var keyAttestation = KeyAttestationBuilder.builder()
        .jwt(JWT)
        .build();
    when(attestationIssuerPort.createKeyAttestation(any(), any())).thenReturn(keyAttestation);
    var keys = List.of(JwkTestBuilder.withDefaults().build());

    var result = attestationService.createKeyAttestation(keys, nonce);

    assertThat(result).isNotNull();
    assertThat(result.jwt()).isEqualTo(JWT);
  }

  @Test
  void serves_wallet_instance_attestation() {

    var wia = WalletInstanceAttestationBuilder.builder()
        .jwt(JWT)
        .build();
    when(attestationIssuerPort.createWalletInstanceAttestation(any())).thenReturn(wia);
    var key = JwkTestBuilder.withDefaults().build();

    var result = attestationService.createWalletInstanceAttestation(key);

    assertThat(result).isNotNull();
    assertThat(result.jwt()).isEqualTo(JWT);
  }
}
