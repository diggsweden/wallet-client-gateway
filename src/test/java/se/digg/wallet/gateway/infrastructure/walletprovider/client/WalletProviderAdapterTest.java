// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.walletprovider.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.digg.wallet.gateway.domain.exception.WalletRuntimeException;
import se.digg.wallet.gateway.client.provider.v0.api.WalletUnitAttestationApi;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.JwkBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class WalletProviderAdapterTest {

  @MockitoBean
  private ObjectMapper objectMapper;

  @MockitoBean
  private WalletUnitAttestationApi walletUnitAttestationApi;

  @Autowired
  private WalletProviderAdapter adapter;

  @Test
  void nullWalletKeyThrowsIllegalArgumentException() {

    assertThrows(IllegalArgumentException.class,
        () -> adapter.createWalletUnitAttestation(null, null));

    verify(walletUnitAttestationApi, never()).postWalletUnitAttestation(any());
  }

  @Test
  void walletKeySerializationFailureThrowsWalletRuntimeException() throws JsonProcessingException {

    when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

    assertThrows(WalletRuntimeException.class,
        () -> adapter.createWalletUnitAttestation(defaultWalletKey(), null));

    verify(walletUnitAttestationApi, never()).postWalletUnitAttestation(any());
  }

  @ParameterizedTest
  @NullAndEmptySource
  void emptyNonceServesWalletUnitAttestation(String nonce) {

    final var expectedJwt = "123456";
    when(walletUnitAttestationApi.postWalletUnitAttestation(any())).thenReturn(expectedJwt);

    var wua =
        assertDoesNotThrow(() -> adapter.createWalletUnitAttestation(defaultWalletKey(), nonce));

    verify(walletUnitAttestationApi, times(1)).postWalletUnitAttestation(any());
    assertThat(wua).isNotNull();
    assertThat(wua.jwt()).isEqualTo(expectedJwt);
  }

  @Test
  void servesWalletUnitAttestation() {

    final var expectedJwt = "123456";
    when(walletUnitAttestationApi.postWalletUnitAttestation(any())).thenReturn(expectedJwt);

    var wua = assertDoesNotThrow(
        () -> adapter.createWalletUnitAttestation(defaultWalletKey(), "some-nonce"));

    verify(walletUnitAttestationApi, times(1)).postWalletUnitAttestation(any());
    assertThat(wua).isNotNull();
    assertThat(wua.jwt()).isEqualTo(expectedJwt);
  }

  private static Jwk defaultWalletKey() {
    return JwkBuilder.builder()
        .kid("some-kid")
        .build();
  }
}
