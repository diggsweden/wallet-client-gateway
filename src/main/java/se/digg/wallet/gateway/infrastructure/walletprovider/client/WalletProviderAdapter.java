// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.walletprovider.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import se.digg.wallet.gateway.application.config.WalletRuntimeException;
import se.digg.wallet.gateway.client.provider.v0.api.WalletUnitAttestationApi;
import se.digg.wallet.gateway.client.provider.v0.model.WalletUnitAttestationRequest;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.wua.Wua;
import se.digg.wallet.gateway.domain.model.wua.WuaBuilder;
import se.digg.wallet.gateway.domain.ports.outbound.WalletProviderPort;

@Component
public class WalletProviderAdapter implements WalletProviderPort {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private WalletUnitAttestationApi walletUnitAttestationApi;

  @Override
  public Wua createWalletUnitAttestation(Jwk walletKey, @Nullable String nonce) {

    Assert.notNull(walletKey, "WalletKey must not be null");
    try {
      var jwkString = objectMapper.writeValueAsString(walletKey);
      var request = WalletUnitAttestationRequest.builder()
          .jwk(jwkString)
          .nonce(nonce)
          .build();

      var createdWua = walletUnitAttestationApi.postWalletUnitAttestation(request);

      return WuaBuilder.builder()
          .jwt(createdWua)
          .build();

    } catch (JsonProcessingException e) {
      throw new WalletRuntimeException(e);
    }
  }
}
