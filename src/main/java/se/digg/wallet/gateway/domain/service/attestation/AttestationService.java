// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.attestation;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.application.model.attestation.AttestationDto;
import se.digg.wallet.gateway.application.model.attestation.AttestationListDto;
import se.digg.wallet.gateway.application.model.attestation.CreateAttestationDto;
import se.digg.wallet.gateway.infrastructure.attestation.client.AttestationsClient;
import se.digg.wallet.gateway.infrastructure.attestation.model.ClientAttestationDto;

@Service
public class AttestationService {

  private final AttestationsClient attestationsClient;

  private final AttestationMapper attestationMapper;

  public AttestationService(AttestationsClient attestationsClient,
      AttestationMapper attestationMapper) {
    this.attestationsClient = attestationsClient;
    this.attestationMapper = attestationMapper;
  }

  public Optional<AttestationDto> createAttestation(CreateAttestationDto createAttestationDto) {
    return toOptionalCaDto(attestationsClient
        .creatAttestation(attestationMapper.toClientAttestationDto(createAttestationDto)));
  }

  public Optional<AttestationDto> getAttestation(UUID attestationId) {
    return toOptionalCaDto(attestationsClient.getAttestation(attestationId));
  }

  public AttestationListDto getAttestationByHsmId(UUID hsmId) {
    return attestationMapper.toAttestationListDto(attestationsClient.getAttestationByHsmId(hsmId));
  }

  private Optional<AttestationDto> toOptionalCaDto(
      Optional<ClientAttestationDto> clientAttestationDto) {
    if (clientAttestationDto.isPresent()) {
      return Optional.of(attestationMapper.toAttestationDto(clientAttestationDto.get()));
    } else {
      return Optional.empty();
    }
  }
}
