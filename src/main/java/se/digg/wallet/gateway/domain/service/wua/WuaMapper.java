// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.wua;

import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.application.config.WalletRuntimeException;
import se.digg.wallet.gateway.application.model.wua.CreateWuaDto;
import se.digg.wallet.gateway.application.model.wua.CreateWuaDtoV2;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountAccountDto;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDto;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDtoV1;
import se.digg.wallet.gateway.infrastructure.walletprovider.model.WalletProviderCreateWuaDtoV2;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class WuaMapper {

  private final ObjectMapper objectMapper;

  public WuaMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper.rebuild().build();
  }

    public WalletProviderCreateWuaDto toWalletProviderCreateWuaDto(WalletAccountAccountDto account) {
        String jwkString;
        try {
            jwkString = objectMapper.writeValueAsString(account.publicKey());
        } catch (JacksonException e) {
            throw new WalletRuntimeException(e);
        }
        return new WalletProviderCreateWuaDto(jwkString);
    }

    @Deprecated(since = "0.3.1", forRemoval = true)
    public WalletProviderCreateWuaDtoV1 toWalletProviderCreateWuaDto(CreateWuaDto createWuaDto) {
        String jwkString;
        try {
            jwkString = objectMapper.writeValueAsString(createWuaDto.jwk());
        } catch (JacksonException e) {
            throw new WalletRuntimeException(e);
        }
        return new WalletProviderCreateWuaDtoV1(createWuaDto.walletId().toString(), jwkString);
    }

  public WalletProviderCreateWuaDtoV2 toWalletProviderCreateWuaDtoV2(CreateWuaDtoV2 createWuaDto) {
    String jwkString;
    try {
      jwkString = objectMapper.writeValueAsString(createWuaDto.jwk());
    } catch (JacksonException e) {
      throw new WalletRuntimeException(e);
    }
    return new WalletProviderCreateWuaDtoV2(createWuaDto.walletId().toString(),
        jwkString,
        createWuaDto.nonce());
  }
}
