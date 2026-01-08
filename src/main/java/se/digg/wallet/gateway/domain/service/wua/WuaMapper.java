// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.wua;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.application.config.WalletRuntimeException;
import se.digg.wallet.gateway.application.model.wua.CreateWuaDto;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDto;

@Component
public class WuaMapper {

  private final ObjectMapper objectMapper;

  public WuaMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper.copy();
  }

  public WalletProviderCreateWuaDto toWalletProviderCreateWuaDto(CreateWuaDto createWuaDto) {
    String jwkString;
    try {
      jwkString = objectMapper.writeValueAsString(createWuaDto.jwk());
    } catch (JsonProcessingException e) {
      throw new WalletRuntimeException(e);
    }
    return new WalletProviderCreateWuaDto(createWuaDto.walletId().toString(), jwkString);
  }
}
