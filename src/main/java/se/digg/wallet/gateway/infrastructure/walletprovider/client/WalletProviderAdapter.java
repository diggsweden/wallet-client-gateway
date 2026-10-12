// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.walletprovider.client;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import se.digg.wallet.gateway.client.provider.v0.api.KeyAttestationApi;
import se.digg.wallet.gateway.client.provider.v0.api.WalletInstanceAttestationApi;
import se.digg.wallet.gateway.client.provider.v0.api.WalletUnitAttestationApi;
import se.digg.wallet.gateway.client.provider.v0.model.KeyAttestationItem;
import se.digg.wallet.gateway.client.provider.v0.model.KeyAttestationRequest;
import se.digg.wallet.gateway.client.provider.v0.model.WalletInstanceAttestationRequest;
import se.digg.wallet.gateway.client.provider.v0.model.WalletUnitAttestationRequest;
import se.digg.wallet.gateway.domain.model.attestation.KeyAttestation;
import se.digg.wallet.gateway.domain.model.attestation.KeyAttestationBuilder;
import se.digg.wallet.gateway.domain.model.attestation.WalletInstanceAttestation;
import se.digg.wallet.gateway.domain.model.attestation.WalletInstanceAttestationBuilder;
import se.digg.wallet.gateway.domain.model.attestation.Wua;
import se.digg.wallet.gateway.domain.model.attestation.WuaBuilder;
import se.digg.wallet.gateway.domain.model.common.Jwk;
import se.digg.wallet.gateway.domain.ports.outbound.WalletProviderPort;

@Component
public class WalletProviderAdapter implements WalletProviderPort {

  @Autowired
  private WalletProviderClientMapper mapper;

  @Autowired
  private WalletUnitAttestationApi walletUnitAttestationApi;

  @Autowired
  private KeyAttestationApi keyAttestationApi;

  @Autowired
  private WalletInstanceAttestationApi walletInstanceAttestationApi;

  @Override
  public Wua createWalletUnitAttestation(Jwk walletKey, @Nullable String nonce) {

    Assert.notNull(walletKey, "WalletKey must not be null");

    var jwkString = mapper.jwkToJsonString(walletKey);
    var request = WalletUnitAttestationRequest.builder()
        .jwk(jwkString)
        .nonce(nonce)
        .build();

    var createdWua = walletUnitAttestationApi.postWalletUnitAttestation(request);

    return WuaBuilder.builder()
        .jwt(createdWua)
        .build();
  }

  @Override
  public KeyAttestation createKeyAttestation(List<Jwk> keys, @Nullable String nonce) {

    var jwkStrings = keys.stream()
        .filter(Objects::nonNull)
        .map(jwk -> mapper.jwkToJsonString(jwk))
        .map(jwkString -> KeyAttestationItem.builder()
            .jwk(jwkString)
            .build())
        .toList();
    var request = KeyAttestationRequest.builder()
        .jwks(jwkStrings)
        .nonce(nonce)
        .build();

    var kaResponse = keyAttestationApi.postKeyAttestation(request);

    return KeyAttestationBuilder.builder()
        .jwt(kaResponse.getKeyAttestation())
        .build();
  }

  @Override
  public WalletInstanceAttestation createWalletInstanceAttestation(Jwk key) {

    Assert.notNull(key, "Key must not be null");

    var jwkString = mapper.jwkToJsonString(key);
    var request = WalletInstanceAttestationRequest.builder()
        .jwk(jwkString)
        .build();

    var wiaResponse = walletInstanceAttestationApi.postWalletInstanceAttestation(request);

    return WalletInstanceAttestationBuilder.builder()
        .jwt(wiaResponse.getWalletInstanceAttestation())
        .build();
  }
}
