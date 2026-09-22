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
import se.digg.wallet.gateway.client.provider.v0.api.WalletUnitAttestationApi;
import se.digg.wallet.gateway.client.provider.v0.model.KeyAttestationItem;
import se.digg.wallet.gateway.client.provider.v0.model.KeyAttestationRequest;
import se.digg.wallet.gateway.client.provider.v0.model.WalletUnitAttestationRequest;
import se.digg.wallet.gateway.domain.model.common.Jwk;
import se.digg.wallet.gateway.domain.model.walletprovider.KeyAttestation;
import se.digg.wallet.gateway.domain.model.walletprovider.Wua;
import se.digg.wallet.gateway.domain.model.walletprovider.WuaBuilder;
import se.digg.wallet.gateway.domain.ports.outbound.WalletProviderPort;

@Component
public class WalletProviderAdapter implements WalletProviderPort {

  @Autowired
  private WalletProviderClientMapper mapper;

  @Autowired
  private WalletUnitAttestationApi walletUnitAttestationApi;

  @Autowired
  private KeyAttestationApi keyAttestationApi;

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

    var response = keyAttestationApi.postKeyAttestation(request);

    return new KeyAttestation(response.getKeyAttestation());
  }
}
