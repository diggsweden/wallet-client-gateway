// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.hsm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.digg.wallet.gateway.api.v0.model.EcPublicJwkDto;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeviceStateRegistrationResult(
    String status,
    String clientId,
    String devAuthorizationCode,
    EcPublicJwkDto serverJwsPublicKey,
    String opaqueServerId) {
}
