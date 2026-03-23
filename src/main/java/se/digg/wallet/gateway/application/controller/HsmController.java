// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;
import se.digg.wallet.gateway.application.model.hsm.HsmRequestDto;
import se.digg.wallet.gateway.application.model.hsm.HsmResponseDto;
import se.digg.wallet.gateway.domain.port.in.HsmUseCase;

@RestController
@RequestMapping("/wallet-security/v1")
public class HsmController {

  private final HsmUseCase hsmUseCase;

  public HsmController(HsmUseCase hsmUseCase) {
    this.hsmUseCase = hsmUseCase;
  }

  @PostMapping("/device/state")
  public ResponseEntity<Void> registerState(
      @AuthenticationPrincipal ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    hsmUseCase.registerState(auth.getAccountId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/device/pin")
  public ResponseEntity<Void> registerPin(
      @AuthenticationPrincipal ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    hsmUseCase.registerPin(auth.getAccountId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/device/pin/change")
  public ResponseEntity<Void> changePin(
      @AuthenticationPrincipal ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    hsmUseCase.changePin(auth.getAccountId(), request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/secure-session")
  public ResponseEntity<HsmResponseDto> createSession(
      @AuthenticationPrincipal ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    var response = hsmUseCase.createSession(auth.getAccountId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/keys")
  public ResponseEntity<HsmResponseDto> createKey(
      @AuthenticationPrincipal ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    var response = hsmUseCase.createKey(auth.getAccountId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/keys/list")
  public ResponseEntity<HsmResponseDto> listKeys(
      @AuthenticationPrincipal ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    var response = hsmUseCase.listKeys(auth.getAccountId(), request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/keys/delete")
  public ResponseEntity<Void> deleteKey(
      @AuthenticationPrincipal ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    hsmUseCase.deleteKey(auth.getAccountId(), request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/keys/sign")
  public ResponseEntity<HsmResponseDto> sign(
      @AuthenticationPrincipal ChallengeResponseAuthentication auth,
      @RequestBody @Valid HsmRequestDto request) {
    var response = hsmUseCase.sign(auth.getAccountId(), request);
    return ResponseEntity.ok(response);
  }

}
