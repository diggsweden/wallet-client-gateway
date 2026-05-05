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
import se.digg.wallet.gateway.domain.service.hsm.HsmService;

@RestController
public class HsmController implements HsmApi {

  private final HsmService hsmService;

  HsmController(HsmService hsmService) {
    this.hsmService = hsmService;
  }

  @Override
  public ResponseEntity<RegisterStateResponseDto> registerState(
      se.digg.wallet.gateway.api.v0.model.RegisterStateRequestDto registerStateRequest) {
    var registerStateRequestDto = toRegisterStateRequestDto(registerStateRequest);
    var registerStateResponseDto = hsmService.registerState(registerStateRequestDto);
    var registerStateResponse = toRegisterStateResponse(registerStateResponseDto);

    return ResponseEntity.status(HttpStatus.CREATED).body(registerStateResponse);
  }

  @Override
  public ResponseEntity<HsmResponseDto> registerPin(HsmRequestDto hsmRequest) {
    var hsmResponseDto = hsmService.registerPin(toHsmRequestDto(hsmRequest));

    return ResponseEntity.status(HttpStatus.CREATED).body(toHsmResponse(hsmResponseDto));
  }

  @Override
  public ResponseEntity<HsmResponseDto> changePin(HsmRequestDto hsmRequest) {
    var hsmResponseDto = hsmService.changePin(toHsmRequestDto(hsmRequest));

    return ResponseEntity.ok().body(toHsmResponse(hsmResponseDto));
  }

  @Override
  public ResponseEntity<HsmResponseDto> createHsmSession(HsmRequestDto hsmRequest) {
    var hsmResponseDto = hsmService.createSession(toHsmRequestDto(hsmRequest));

    return ResponseEntity.status(HttpStatus.CREATED).body(toHsmResponse(hsmResponseDto));
  }

  @Override
  public ResponseEntity<HsmResponseDto> createKey(HsmRequestDto hsmRequest) {
    var hsmResponseDto = hsmService.createKey(toHsmRequestDto(hsmRequest));

    return ResponseEntity.status(HttpStatus.CREATED).body(toHsmResponse(hsmResponseDto));
  }

  @Override
  public ResponseEntity<Void> deleteKey(HsmRequestDto hsmRequest) {
    hsmService.deleteKey(toHsmRequestDto(hsmRequest));

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<HsmResponseDto> listKeys(HsmRequestDto hsmRequest) {
    var hsmResponseDto = hsmService.listKeys(toHsmRequestDto(hsmRequest));

    return ResponseEntity.ok(toHsmResponse(hsmResponseDto));
  }

  @Override
  public ResponseEntity<HsmResponseDto> sign(HsmRequestDto hsmRequest) {
    var hsmResponseDto = hsmService.sign(toHsmRequestDto(hsmRequest));

    return ResponseEntity.ok(toHsmResponse(hsmResponseDto));
  }

  private static se.digg.wallet.gateway.application.model.hsm.RegisterStateRequestDto toRegisterStateRequestDto(
      se.digg.wallet.gateway.api.v0.model.RegisterStateRequestDto registerStateRequest) {
    var publicKeyRequest = registerStateRequest.getPublicKey();
    var publicKeyDto = new se.digg.wallet.gateway.application.model.hsm.EcPublicJwkDto(
        publicKeyRequest.getKty(),
        publicKeyRequest.getCrv(),
        publicKeyRequest.getX(),
        publicKeyRequest.getY(),
        publicKeyRequest.getKid().orElse(""));

    return new se.digg.wallet.gateway.application.model.hsm.RegisterStateRequestDto(
        publicKeyDto,
        registerStateRequest.getOverwrite(),
        registerStateRequest.getTtl());
  }

  private static RegisterStateResponseDto toRegisterStateResponse(
      se.digg.wallet.gateway.application.model.hsm.RegisterStateResponseDto registerStateResponseDto) {
    return RegisterStateResponseDto.builder()
        .status(registerStateResponseDto.status())
        .clientId(registerStateResponseDto.clientId())
        .devAuthorizationCode(registerStateResponseDto.devAuthorizationCode())
        .build();
  }

  private static se.digg.wallet.gateway.application.model.hsm.HsmRequestDto toHsmRequestDto(
      HsmRequestDto hsmRequest) {
    return new se.digg.wallet.gateway.application.model.hsm.HsmRequestDto(
        hsmRequest.getJwt(),
        hsmRequest.getClientId());
  }

  private static HsmResponseDto toHsmResponse(
      se.digg.wallet.gateway.application.model.hsm.HsmResponseDto hsmResponseDto) {
    return HsmResponseDto.builder()
        .jwt(hsmResponseDto.jwt())
        .build();
  }
}
