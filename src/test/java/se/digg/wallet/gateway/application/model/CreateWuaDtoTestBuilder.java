// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

import java.util.UUID;

public class CreateWuaDtoTestBuilder {

  public static CreateWuaDto withDefaultValues() {
    return withWalletId(UUID.randomUUID());
  }

  public static CreateWuaDto withWalletId(UUID walletId) {
    return new CreateWuaDto(walletId, new JwkDto("kty", "kid", "alg", "use", "crv", "x", "y"));
  }

  public static CreateWuaDto invaliDto() {
    return new CreateWuaDto(UUID.randomUUID(), new JwkDto("", "kid", "alg", "use", "", "x", "y"));
  }
}
