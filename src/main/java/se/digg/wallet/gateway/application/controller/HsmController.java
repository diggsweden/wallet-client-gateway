// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.HsmApi;
import se.digg.wallet.gateway.api.v0.model.HsmRequestDto;
import se.digg.wallet.gateway.api.v0.model.HsmResponseDto;
import se.digg.wallet.gateway.api.v0.model.RegisterStateResponseDto;
import se.digg.wallet.gateway.application.mapper.hsm.HsmMapper;
import se.digg.wallet.gateway.domain.service.hsm.HsmService;

@RestController
public class HsmController implements HsmApi {

  private final HsmService hsmService;
  private final HsmMapper mapper;

  HsmController(HsmService hsmService, HsmMapper mapper) {
    this.hsmService = hsmService;
    this.mapper = mapper;
  }

  @Override
  public ResponseEntity<RegisterStateResponseDto> registerState(
      se.digg.wallet.gateway.api.v0.model.RegisterStateRequestDto registerStateRequest) {
    var registerStateResponse = mapper.toResponse(
        hsmService.registerState(mapper.toDomain(registerStateRequest)));

    return ResponseEntity.status(HttpStatus.CREATED).body(registerStateResponse);
  }

  @Override
  public ResponseEntity<HsmResponseDto> registerPin(HsmRequestDto hsmRequest) {
    var hsmResponseDto = hsmService.registerPin(mapper.toDomain(hsmRequest));

    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(hsmResponseDto));
  }

  @Override
  public ResponseEntity<HsmResponseDto> changePin(HsmRequestDto hsmRequest) {
    var hsmResponseDto = hsmService.changePin(mapper.toDomain(hsmRequest));

    return ResponseEntity.ok().body(mapper.toResponse(hsmResponseDto));
  }

  @Override
  public ResponseEntity<HsmResponseDto> createHsmSession(HsmRequestDto hsmRequest) {
    var hsmResponseDto = hsmService.createSession(mapper.toDomain(hsmRequest));

    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(hsmResponseDto));
  }

  @Override
  public ResponseEntity<HsmResponseDto> createKey(HsmRequestDto hsmRequest) {
    var hsmResponseDto = hsmService.createKey(mapper.toDomain(hsmRequest));

    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(hsmResponseDto));
  }

  @Override
  public ResponseEntity<Void> deleteKey(HsmRequestDto hsmRequest) {
    hsmService.deleteKey(mapper.toDomain(hsmRequest));

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<HsmResponseDto> listKeys(HsmRequestDto hsmRequest) {
    var hsmResponseDto = hsmService.listKeys(mapper.toDomain(hsmRequest));

    return ResponseEntity.ok(mapper.toResponse(hsmResponseDto));
  }

  @Override
  public ResponseEntity<HsmResponseDto> sign(HsmRequestDto hsmRequest) {
    var hsmResponseDto = hsmService.sign(mapper.toDomain(hsmRequest));

    return ResponseEntity.ok(mapper.toResponse(hsmResponseDto));
  }
}
