// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record ValidateAuthChallengeResponseDto(
    @Schema(
        description = "Session ID",
        example = "3f2e7c0b-91d2-4a7e-9f34-0d8d9a5a9e77",
        requiredMode = Schema.RequiredMode.REQUIRED) String sessionId) {
}
