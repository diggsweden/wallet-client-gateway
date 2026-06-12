// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.hsm;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record DeviceStateRegistrationResult(
    String status,
    String clientId,
    String devAuthorizationCode,
    EcPublicJwk serverJwsPublicKey,
    String opaqueServerId,
    String stateJws) {
}
