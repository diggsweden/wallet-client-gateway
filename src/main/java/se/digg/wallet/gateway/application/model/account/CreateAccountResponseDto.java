// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model.account;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record CreateAccountResponseDto(
    @Schema(
        description = "Account ID",
        example = "3685BCB1-4AA4-4C81-8806-43FBD31A0957",
        requiredMode = Schema.RequiredMode.REQUIRED) UUID accountId) {
}
