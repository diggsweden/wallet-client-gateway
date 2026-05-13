// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.model;

public record R2PSEcPublicJwkDto(String kty, String crv, String x, String y, String kid) {
}
