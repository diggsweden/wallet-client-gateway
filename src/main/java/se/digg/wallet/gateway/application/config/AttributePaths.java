// SPDX-FileCopyrightText: 2025 diggsweden/wallet-attribute-attestation
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import jakarta.validation.constraints.NotBlank;

public record AttributePaths(@NotBlank String post, @NotBlank String getById,
    @NotBlank String getByUser) {
}
