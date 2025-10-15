// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.attestation;

import java.util.List;
import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.application.model.attestation.AttestationDto;
import se.digg.wallet.gateway.application.model.attestation.AttestationListDto;
import se.digg.wallet.gateway.application.model.attestation.CreateAttestationDto;
import se.digg.wallet.gateway.infrastructure.attestation.model.ClientAttestationDto;
import se.digg.wallet.gateway.infrastructure.attestation.model.ClientAttestationListDto;

@Component
public class AttestationMapper {

  public ClientAttestationDto toClientAttestationDto(CreateAttestationDto createAttestationDto) {
    return new ClientAttestationDto(
        null,
        createAttestationDto.hsmId(),
        createAttestationDto.wuaId(),
        createAttestationDto.attestationData());
  }

  public AttestationDto toAttestationDto(ClientAttestationDto clientAttestationDto) {
    return new AttestationDto(
        clientAttestationDto.id(),
        clientAttestationDto.hsmId(),
        clientAttestationDto.wuaId(),
        clientAttestationDto.attestationData());
  }

  public AttestationListDto toAttestationListDto(ClientAttestationListDto attestationListDto) {
    List<AttestationDto> attestations =
        attestationListDto.attestations().stream()
            .map(this::toAttestationDto)
            .toList();
    return new AttestationListDto(attestations, attestationListDto.hsmId());

  }
}
