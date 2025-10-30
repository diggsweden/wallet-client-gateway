// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model.wua;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import se.digg.wallet.gateway.application.model.common.JwkDto;

public record CreateWuaDto(@NotNull(message = "Wallet id cannot be empty") UUID walletId,
    @Valid @NotNull(message = "A valid JWK is required") JwkDto jwk) {
}
