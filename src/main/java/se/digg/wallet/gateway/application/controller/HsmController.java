// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.HsmApi;
import se.digg.wallet.gateway.api.v0.model.HsmRequestDto;
import se.digg.wallet.gateway.api.v0.model.HsmResponseDto;
import se.digg.wallet.gateway.api.v0.model.RegisterStateResponseDto;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;
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
    var authentication = getAuthentication();
    var accountId = authentication
        .map(ChallengeResponseAuthentication::getAccountId)
        .orElseThrow();
    var registerStateRequestDto = toRegisterStateRequestDto(registerStateRequest);
    var registerStateResponseDto = hsmService.registerState(accountId, registerStateRequestDto);
    var registerStateResponse = toRegisterStateResponse(registerStateResponseDto);

    return ResponseEntity.status(HttpStatus.CREATED).body(registerStateResponse);
  }

  @Override
  public ResponseEntity<HsmResponseDto> registerPin(HsmRequestDto hsmRequest) {
    var hsmRequestDto = toHsmRequestDto(hsmRequest);
    hsmService.registerPin(getAccountId(), hsmRequestDto);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Override
  public ResponseEntity<HsmResponseDto> changePin(HsmRequestDto hsmRequest) {
    var hsmRequestDto = toHsmRequestDto(hsmRequest);
    var hsmResponseDto = hsmService.changePin(getAccountId(), hsmRequestDto);
    var hsmResponse = toHsmResponse(hsmResponseDto);

    return ResponseEntity.ok().body(hsmResponse);
  }

  @Override
  public ResponseEntity<HsmResponseDto> createHsmSession(HsmRequestDto hsmRequest) {
    var hsmRequestDto = toHsmRequestDto(hsmRequest);
    var hsmResponseDto = hsmService.createSession(getAccountId(), hsmRequestDto);
    var hsmResponse = toHsmResponse(hsmResponseDto);

    return ResponseEntity.status(HttpStatus.CREATED).body(hsmResponse);
  }

  @Override
  public ResponseEntity<HsmResponseDto> createKey(HsmRequestDto hsmRequest) {
    var hsmRequestDto = toHsmRequestDto(hsmRequest);
    var hsmResponseDto = hsmService.createKey(getAccountId(), hsmRequestDto);
    var hsmResponse = toHsmResponse(hsmResponseDto);

    return ResponseEntity.status(HttpStatus.CREATED).body(hsmResponse);
  }

  @Override
  public ResponseEntity<Void> deleteKey(HsmRequestDto hsmRequest) {
    var hsmRequestDto = toHsmRequestDto(hsmRequest);
    hsmService.deleteKey(getAccountId(), hsmRequestDto);

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<HsmResponseDto> listKeys(HsmRequestDto hsmRequest) {
    var hsmRequestDto = toHsmRequestDto(hsmRequest);
    var hsmResponseDto = hsmService.listKeys(getAccountId(), hsmRequestDto);
    var hsmResponse = toHsmResponse(hsmResponseDto);

    return ResponseEntity.ok(hsmResponse);
  }

  @Override
  public ResponseEntity<HsmResponseDto> sign(HsmRequestDto hsmRequest) {
    var hsmRequestDto = toHsmRequestDto(hsmRequest);
    var hsmResponseDto = hsmService.sign(getAccountId(), hsmRequestDto);
    var hsmResponse = toHsmResponse(hsmResponseDto);

    return ResponseEntity.ok(hsmResponse);
  }

  private static String getAccountId() {
    return getAuthentication()
        .map(ChallengeResponseAuthentication::getAccountId)
        .orElseThrow();
  }

  private static Optional<ChallengeResponseAuthentication> getAuthentication() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    return (authentication instanceof ChallengeResponseAuthentication auth) ? Optional.of(auth)
        : Optional.empty();
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
        registerStateRequest.getTtl().orElse(""));
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
        hsmRequest.getJwt());
  }

  private static HsmResponseDto toHsmResponse(
      se.digg.wallet.gateway.application.model.hsm.HsmResponseDto hsmResponseDto) {
    return HsmResponseDto.builder()
        .jwt(hsmResponseDto.jwt())
        .build();
  }
}
