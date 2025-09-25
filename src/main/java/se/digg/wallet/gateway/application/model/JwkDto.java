// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

public record JwkDto(
    String kty,
    String kid,
    String alg,
    String use) {
}
