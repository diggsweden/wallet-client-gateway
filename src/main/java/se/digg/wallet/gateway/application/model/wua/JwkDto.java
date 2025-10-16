// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model.wua;

import jakarta.validation.constraints.NotBlank;

public record JwkDto(
    @NotBlank(message = "kty cannot be blank") String kty,
    String kid,
    String alg,
    String use,
    @NotBlank(message = "crv cannot be blank") String crv,
    @NotBlank(message = "x cannot be blank") String x,
    @NotBlank(message = "y cannot be blank") String y) {
}
