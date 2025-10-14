// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.application.model.CreateAttestationDto;
import se.digg.wallet.gateway.infrastructure.attestation.client.AttestationsClient;
import se.digg.wallet.gateway.infrastructure.attestation.model.AttestationDto;

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
    return attestationsClient
        .creatAttestation(attestationMapper.toAttestationDto(createAttestationDto));
  }
}
