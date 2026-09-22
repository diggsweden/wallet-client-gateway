// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.walletprovider.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.digg.wallet.gateway.client.provider.v0.api.KeyAttestationApi;
import se.digg.wallet.gateway.domain.common.JwkTestBuilder;
import se.digg.wallet.gateway.domain.exception.WalletRuntimeException;
import se.digg.wallet.gateway.client.provider.v0.api.WalletUnitAttestationApi;
import se.digg.wallet.gateway.domain.model.common.Jwk;
import se.digg.wallet.gateway.domain.model.common.JwkBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
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

  @MockitoBean
  private KeyAttestationApi keyAttestationApi;

  @Autowired
  private WalletProviderAdapter adapter;

  @Test
  void serving_wallet_unit_attestation_without_key_throws_illegal_argument_exception() {

    assertThrows(IllegalArgumentException.class,
        () -> adapter.createWalletUnitAttestation(null, null));

    verify(walletUnitAttestationApi, never()).postWalletUnitAttestation(any());
  }

  @Test
  void json_processing_failure_with_wallet_unit_attestation_throws_wallet_runtime_exception() {

    try {
      when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
    } catch (JsonProcessingException e) {
      fail(e);
    }

    assertThrows(WalletRuntimeException.class,
        () -> adapter.createWalletUnitAttestation(defaultWalletKey(), null));

    verify(walletUnitAttestationApi, never()).postWalletUnitAttestation(any());
  }

  @ParameterizedTest
  @NullAndEmptySource
  void serves_wallet_unit_attestation_without_nonce(String nonce) {

    final var expectedJwt = "123456";
    when(walletUnitAttestationApi.postWalletUnitAttestation(any())).thenReturn(expectedJwt);

    var wua =
        assertDoesNotThrow(() -> adapter.createWalletUnitAttestation(defaultWalletKey(), nonce));

    verify(walletUnitAttestationApi, times(1)).postWalletUnitAttestation(any());
    assertThat(wua).isNotNull();
    assertThat(wua.jwt()).isEqualTo(expectedJwt);
  }

  @Test
  void serves_wallet_unit_attestation_with_nonce() {

    final var expectedJwt = "123456";
    when(walletUnitAttestationApi.postWalletUnitAttestation(any())).thenReturn(expectedJwt);

    var wua = assertDoesNotThrow(
        () -> adapter.createWalletUnitAttestation(defaultWalletKey(), "some-nonce"));

    verify(walletUnitAttestationApi, times(1)).postWalletUnitAttestation(any());
    assertThat(wua).isNotNull();
    assertThat(wua.jwt()).isEqualTo(expectedJwt);
  }

  @Test
  void requesting_key_attestation_with_null_keys_throws_null_pointer_exception() {

    assertThrows(NullPointerException.class,
        () -> adapter.createKeyAttestation(null, null));

    verify(keyAttestationApi, never()).postKeyAttestation(any());
  }

  @Test
  void json_processing_failure_with_key_attestation_throws_wallet_runtime_exception()
      throws JsonProcessingException {

    when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

    assertThrows(WalletRuntimeException.class,
        () -> adapter.createKeyAttestation(List.of(defaultWalletKey()), null));

    verify(keyAttestationApi, never()).postKeyAttestation(any());
  }

  @ParameterizedTest
  @NullAndEmptySource
  void serves_key_attestation_without_nonce(String nonce) {

    final var keyAttestationResponse = defaultKeyAttestationResponse();
    when(keyAttestationApi.postKeyAttestation(any())).thenReturn(keyAttestationResponse);

    var result = adapter.createKeyAttestation(
        List.of(JwkTestBuilder.withDefaults().build()), nonce);

    verify(keyAttestationApi, times(1)).postKeyAttestation(any());
    assertThat(result).isNotNull();
    assertThat(result.jwt()).isEqualTo(keyAttestationResponse.getKeyAttestation());
  }

  @Test
  void serves_key_attestation_with_nonce() {

    final var keyAttestationResponse = defaultKeyAttestationResponse();
    when(keyAttestationApi.postKeyAttestation(any())).thenReturn(keyAttestationResponse);

    var result = adapter.createKeyAttestation(
        List.of(JwkTestBuilder.withDefaults().build()), "some-nonce");

    verify(keyAttestationApi, times(1)).postKeyAttestation(any());
    assertThat(result).isNotNull();
    assertThat(result.jwt()).isEqualTo(keyAttestationResponse.getKeyAttestation());
  }

  private static Jwk defaultWalletKey() {
    return JwkBuilder.builder()
        .kid("some-kid")
        .build();
  }

  private static se.digg.wallet.gateway.client.provider.v0.model.KeyAttestationResponse defaultKeyAttestationResponse() {
    return se.digg.wallet.gateway.client.provider.v0.model.KeyAttestationResponse.builder()
        .keyAttestation("123456")
        .build();
  }
}
