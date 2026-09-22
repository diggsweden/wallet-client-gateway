// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.walletprovider.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import se.digg.wallet.gateway.domain.exception.WalletRuntimeException;
import se.digg.wallet.gateway.domain.model.common.Jwk;

@Component
public class WalletProviderClientMapper {

  @Autowired
  private ObjectMapper objectMapper;

  String jwkToJsonString(Jwk jwk) {

    Assert.notNull(jwk, "The JWK must not be null");

    try {
      return objectMapper.writeValueAsString(jwk);
    } catch (JsonProcessingException e) {
      throw new WalletRuntimeException("Unable to serialize JWK to JSON string", e);
    }
  }
}
