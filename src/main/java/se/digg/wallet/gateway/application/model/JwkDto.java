// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

import jakarta.validation.constraints.NotBlank;

public record JwkDto(
    @NotBlank String kty,
    @NotBlank String kid,
    @NotBlank String alg,
    @NotBlank String use,
    @NotBlank String crv,
    @NotBlank String x, 
    @NotBlank String y
    ) {
}
