// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.wua;

import io.soabase.recordbuilder.core.RecordBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

@RecordBuilder
public record Wua(
    @Schema(
        description = "Wallet unit attestation",
        example = "ey5359ddf330...",
        requiredMode = Schema.RequiredMode.REQUIRED) String jwt) {
}
