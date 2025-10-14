// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.application.model.CreateAttestationDto;
import se.digg.wallet.gateway.infrastructure.attestation.model.AttestationDto;

@Component
public class AttestationMapper {

  public AttestationDto toAttestationDto(CreateAttestationDto createAttestationDto) {
    return new AttestationDto(
        null,
        createAttestationDto.hsmId(),
        createAttestationDto.wuaId(),
        createAttestationDto.attestationData());
  }
}
