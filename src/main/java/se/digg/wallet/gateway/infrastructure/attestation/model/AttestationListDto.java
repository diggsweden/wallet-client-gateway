// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.attestation.model;

import java.util.List;
import java.util.UUID;

public record AttestationListDto(List<AttestationDto> attestations, UUID hsmId) {
}
