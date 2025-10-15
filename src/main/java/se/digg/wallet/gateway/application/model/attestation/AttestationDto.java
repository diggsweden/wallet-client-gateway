// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model.attestation;

import java.util.UUID;

public record AttestationDto(
    UUID id,
    UUID hsmId,
    UUID wuaId,
    String attestationData) {
}

