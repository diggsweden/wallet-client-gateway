// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.account.model;

public record WalletAccountJwkDto(
    String kty,
    String kid,
    String alg,
    String use,
    String crv,
    String x,
    String y) {
}
