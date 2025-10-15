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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.controller.openapi.attestation.GetListOpenApiDocumentation;
import se.digg.wallet.gateway.application.controller.openapi.attestation.GetOpenApiDocumentation;
import se.digg.wallet.gateway.application.controller.openapi.attestation.PostOpenApiDocumentation;
import se.digg.wallet.gateway.application.model.attestation.AttestationDto;
import se.digg.wallet.gateway.application.model.attestation.AttestationListDto;
import se.digg.wallet.gateway.application.model.attestation.CreateAttestationDto;
import se.digg.wallet.gateway.domain.service.attestation.AttestationService;

@RestController
@RequestMapping("/attribute-attestations")
public class AttestationController {
  private final AttestationService attetstationService;

  public AttestationController(AttestationService attetstationService) {
    this.attetstationService = attetstationService;
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
    return attetstationService.getAttestation(id).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  @GetListOpenApiDocumentation
  public ResponseEntity<AttestationListDto> getAttestationsById(@RequestParam UUID key) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(attetstationService.getAttestationByHsmId(key));
  }
}
