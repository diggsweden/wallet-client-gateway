// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.controller.openapi.attestation.GetListOpenApiDocumentation;
import se.digg.wallet.gateway.application.controller.openapi.attestation.GetOpenApiDocumentation;
import se.digg.wallet.gateway.application.controller.openapi.attestation.PostOpenApiDocumentation;
import se.digg.wallet.gateway.application.model.CreateAttestationDto;
import se.digg.wallet.gateway.domain.service.AttestationService;
import se.digg.wallet.gateway.infrastructure.attestation.client.AttestationsClient;
import se.digg.wallet.gateway.infrastructure.attestation.model.AttestationDto;
import se.digg.wallet.gateway.infrastructure.attestation.model.AttestationListDto;

@RestController
@RequestMapping("/attestation")
public class AttestationController {
  private final AttestationService attetstationService;
  private final AttestationsClient attestationsClient;

  public AttestationController(AttestationService attetstationService,
      AttestationsClient attestationsClient) {
    this.attetstationService = attetstationService;
    this.attestationsClient = attestationsClient;
  }

  @PostMapping
  @PostOpenApiDocumentation
  public ResponseEntity<AttestationDto> createAttribute(
      @RequestBody @Valid CreateAttestationDto attestationDto) {
    Optional<AttestationDto> attestation =
        attetstationService.createAttestation(attestationDto);
    if (attestation.isPresent()) {
      return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(attestation.get());
    } else {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{id}")
  @GetOpenApiDocumentation
  public ResponseEntity<AttestationDto> getAttestationById(@PathVariable final UUID id) {
    return attestationsClient.getAttestation(id).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/users/{hsmId}")
  @GetListOpenApiDocumentation
  public ResponseEntity<AttestationListDto> getAttestationsById(@PathVariable UUID hsmId) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(attestationsClient.getAttestationByHsmId(hsmId));
  }
}
