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
import se.digg.wallet.gateway.api.v0.model.RegisterStateRequestDto;
import se.digg.wallet.gateway.api.v0.model.RegisterStateResponseDto;
import se.digg.wallet.gateway.application.mapper.hsm.HsmMapper;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResult;
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
      RegisterStateRequestDto registerStateRequest) {
    DeviceStateRegistration stateRegistration = mapper.toDomain(registerStateRequest);
    DeviceStateRegistrationResult stateRegistrationResult =
        hsmService.registerState(stateRegistration);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(mapper.toResponse(stateRegistrationResult));
  }

  @Override
  public ResponseEntity<HsmResponseDto> registerPin(HsmRequestDto hsmRequest) {
    HsmOperation hsmOperation = mapper.toDomain(hsmRequest);
    HsmOperationResult hsmOperationResult = hsmService.registerPin(hsmOperation);

    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(hsmOperationResult));
  }

  @Override
  public ResponseEntity<HsmResponseDto> changePin(HsmRequestDto hsmRequest) {
    HsmOperation hsmOperation = mapper.toDomain(hsmRequest);
    HsmOperationResult hsmOperationResult = hsmService.changePin(hsmOperation);

    return ResponseEntity.ok().body(mapper.toResponse(hsmOperationResult));
  }

  @Override
  public ResponseEntity<HsmResponseDto> createHsmSession(HsmRequestDto hsmRequest) {
    HsmOperation hsmOperation = mapper.toDomain(hsmRequest);
    HsmOperationResult hsmOperationResult = hsmService.createSession(hsmOperation);

    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(hsmOperationResult));
  }

  @Override
  public ResponseEntity<HsmResponseDto> createKey(HsmRequestDto hsmRequest) {
    HsmOperation hsmOperation = mapper.toDomain(hsmRequest);
    HsmOperationResult hsmOperationResult = hsmService.createKey(hsmOperation);

    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(hsmOperationResult));
  }

  @Override
  public ResponseEntity<Void> deleteKey(HsmRequestDto hsmRequest) {
    HsmOperation hsmOperation = mapper.toDomain(hsmRequest);
    hsmService.deleteKey(hsmOperation);

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<HsmResponseDto> listKeys(HsmRequestDto hsmRequest) {
    HsmOperation hsmOperation = mapper.toDomain(hsmRequest);
    HsmOperationResult hsmOperationResult = hsmService.listKeys(hsmOperation);

    return ResponseEntity.ok(mapper.toResponse(hsmOperationResult));
  }

  @Override
  public ResponseEntity<HsmResponseDto> sign(HsmRequestDto hsmRequest) {
    HsmOperation hsmOperation = mapper.toDomain(hsmRequest);
    HsmOperationResult hsmOperationResult = hsmService.sign(hsmOperation);

    return ResponseEntity.ok(mapper.toResponse(hsmOperationResult));
  }
}
