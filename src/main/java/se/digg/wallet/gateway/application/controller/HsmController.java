// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;
import se.digg.wallet.gateway.application.model.hsm.HsmRequestDto;
import se.digg.wallet.gateway.application.model.hsm.HsmResponseDto;
import se.digg.wallet.gateway.application.model.hsm.RegisterStateRequestDto;
import se.digg.wallet.gateway.application.model.hsm.RegisterStateResponseDto;
import se.digg.wallet.gateway.domain.port.in.HsmUseCase;

@RestController
@RequestMapping("/wallet-security/v1")
public class HsmController {

  private final HsmUseCase hsmUseCase;

  HsmController(HsmUseCase hsmUseCase) {
    this.hsmUseCase = hsmUseCase;
  }

  @PostMapping("/device/state")
  public ResponseEntity<RegisterStateResponseDto> registerState(
      @Valid ChallengeResponseAuthentication auth,
      @RequestBody @Valid RegisterStateRequestDto request) {
    var response = hsmUseCase.registerState(auth.getAccountId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/device/pin")
  public ResponseEntity<Void> registerPin(
      @Valid ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    hsmUseCase.registerPin(auth.getAccountId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/device/pin/change")
  public ResponseEntity<Void> changePin(
      @Valid ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    hsmUseCase.changePin(auth.getAccountId(), request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/secure-session")
  public ResponseEntity<HsmResponseDto> createSession(
      @Valid ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    var response = hsmUseCase.createSession(auth.getAccountId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/keys")
  public ResponseEntity<HsmResponseDto> createKey(
      @Valid ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    var response = hsmUseCase.createKey(auth.getAccountId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/keys/list")
  public ResponseEntity<HsmResponseDto> listKeys(
      @Valid ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    var response = hsmUseCase.listKeys(auth.getAccountId(), request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/keys/delete")
  public ResponseEntity<Void> deleteKey(
      @Valid ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    hsmUseCase.deleteKey(auth.getAccountId(), request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/keys/sign")
  public ResponseEntity<HsmResponseDto> sign(
      @Valid ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    var response = hsmUseCase.sign(auth.getAccountId(), request);
    return ResponseEntity.ok(response);
  }

}
