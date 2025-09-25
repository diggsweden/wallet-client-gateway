// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.application.model.CreateWuaDto;
import se.digg.wallet.gateway.application.model.WuaDto;
import se.digg.wallet.gateway.infrastructure.walletprovider.client.WalletProviderClient;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDto;

@Service
public class WuaService {

  private final WalletProviderClient walletProviderClient;
  private final ObjectMapper objectMapper;

  public WuaService(WalletProviderClient walletProviderClient, ObjectMapper objectMapper) {
    this.walletProviderClient = walletProviderClient;
    this.objectMapper = objectMapper;
  }

  public WuaDto createWua(CreateWuaDto createWuaDto) {
    String jwkString;
    try {
      jwkString = objectMapper.writeValueAsString(createWuaDto.jwk());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    var mapped = new WalletProviderCreateWuaDto(createWuaDto.walletId().toString(), jwkString);
    var result = walletProviderClient.createWua(mapped);
    return new WuaDto(result);
  }
}
