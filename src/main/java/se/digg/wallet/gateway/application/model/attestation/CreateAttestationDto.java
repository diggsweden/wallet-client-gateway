// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model.attestation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAttestationDto(
    @NotNull UUID hsmId,
    @NotNull UUID wuaId,
    @NotBlank String attestationData) {

}
