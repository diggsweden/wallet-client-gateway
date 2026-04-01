// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.AttestationApi;
import se.digg.wallet.gateway.api.v0.model.ClientAttestationDto;
import se.digg.wallet.gateway.api.v0.model.ClientAttestationListDto;
import se.digg.wallet.gateway.api.v0.model.CreateAttestationDto;
import se.digg.wallet.gateway.domain.service.attestation.AttestationService;

@RestController
public class AttestationController implements AttestationApi {
  private final AttestationService attetstationService;

  public AttestationController(AttestationService attetstationService) {
    this.attetstationService = attetstationService;
  }

  @Override
  public ResponseEntity<ClientAttestationDto> createAttestation(
      CreateAttestationDto createAttestationRequest) {

    var createAttestationDto = toCreateAttestationDto(createAttestationRequest);

    return attetstationService.createAttestation(createAttestationDto)
        .map(AttestationController::toClientAttestationResponse)
        .map(clientAttestationDto -> ResponseEntity
            .status(HttpStatus.CREATED)
            .body(clientAttestationDto))
        .orElse(ResponseEntity.badRequest().build());
  }

  @Override
  public ResponseEntity<ClientAttestationDto> getAttestationById(UUID id) {

    return attetstationService.getAttestation(id)
        .map(AttestationController::toClientAttestationResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<ClientAttestationListDto> getAttestationsById(UUID key) {

    var clientAttestationListDto = attetstationService.getAttestationByHsmId(key);
    var clientAttestationListResponse = toClientAttestationListResponse(clientAttestationListDto);
    return ResponseEntity.ok(clientAttestationListResponse);
  }

  private static se.digg.wallet.gateway.application.model.attestation.CreateAttestationDto toCreateAttestationDto(
      CreateAttestationDto createAttestationRequest) {
    return new se.digg.wallet.gateway.application.model.attestation.CreateAttestationDto(
        createAttestationRequest.getHsmId(),
        createAttestationRequest.getWuaId(),
        createAttestationRequest.getAttestationData());
  }

  private static ClientAttestationDto toClientAttestationResponse(
      se.digg.wallet.gateway.application.model.attestation.AttestationDto clientAttestationResponseDto) {
    return ClientAttestationDto.builder()
        .id(clientAttestationResponseDto.id())
        .hsmId(clientAttestationResponseDto.hsmId())
        .wuaId(clientAttestationResponseDto.wuaId())
        .attestationData(clientAttestationResponseDto.attestationData())
        .build();
  }

  private static ClientAttestationListDto toClientAttestationListResponse(
      se.digg.wallet.gateway.application.model.attestation.AttestationListDto clientAttestationListDto) {
    var clientAttestations = clientAttestationListDto.attestations().stream()
        .map(AttestationController::toClientAttestationResponse)
        .toList();

    return ClientAttestationListDto.builder()
        .hsmId(clientAttestationListDto.hsmId())
        .attestations(clientAttestations)
        .build();
  }
}
