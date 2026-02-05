// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

import java.util.UUID;

import se.digg.wallet.gateway.application.model.common.JwkDto;
import se.digg.wallet.gateway.application.model.wua.CreateWuaDto;

public class CreateWuaDtoTestBuilder {

  public static CreateWuaDto withWalletId(UUID walletId) {
    return withWalletId(walletId, JwkDtoTestBuilder.withDefaults().build());
  }

  public static CreateWuaDto withWalletId(UUID walletId, JwkDto jwkDto) {
    return new CreateWuaDto(walletId, jwkDto);
  }

  public static CreateWuaDto invalidDto() {
    return new CreateWuaDto(UUID.randomUUID(), JwkDtoTestBuilder.withDefaults()
        .kty("")
        .build());
  }
}
