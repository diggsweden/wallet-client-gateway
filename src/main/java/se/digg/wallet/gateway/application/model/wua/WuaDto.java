// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model.wua;

import io.swagger.v3.oas.annotations.media.Schema;

public record WuaDto(
    @Schema(
        description = "Wallet unit attestation",
        example = "ey5359ddf330...",
        requiredMode = Schema.RequiredMode.REQUIRED) String jwt) {
}
