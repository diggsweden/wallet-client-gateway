// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.HsmAsyncApi;
import se.digg.wallet.gateway.api.v0.model.AsyncHsmResponseDto;
import se.digg.wallet.gateway.api.v0.model.HsmRequestDto;
import se.digg.wallet.gateway.application.mapper.hsm.HsmMapper;
import se.digg.wallet.gateway.domain.model.hsm.AsyncHsmOperationResult;
import se.digg.wallet.gateway.domain.service.hsm.HsmService;

@RestController
public class HsmAsyncController implements HsmAsyncApi {

  private final HsmService hsmService;
  private final HsmMapper mapper;

  HsmAsyncController(HsmService hsmService, HsmMapper mapper) {
    this.hsmService = hsmService;
    this.mapper = mapper;
  }

  @Override
  public ResponseEntity<AsyncHsmResponseDto> registerPinAsync(HsmRequestDto hsmRequest) {
    return submit(hsmRequest);
  }

  @Override
  public ResponseEntity<AsyncHsmResponseDto> changePinAsync(HsmRequestDto hsmRequest) {
    return submit(hsmRequest);
  }

  @Override
  public ResponseEntity<AsyncHsmResponseDto> createHsmSessionAsync(HsmRequestDto hsmRequest) {
    return submit(hsmRequest);
  }

  @Override
  public ResponseEntity<AsyncHsmResponseDto> createKeyAsync(HsmRequestDto hsmRequest) {
    return submit(hsmRequest);
  }

  @Override
  public ResponseEntity<AsyncHsmResponseDto> deleteKeyAsync(HsmRequestDto hsmRequest) {
    return submit(hsmRequest);
  }

  @Override
  public ResponseEntity<AsyncHsmResponseDto> listKeysAsync(HsmRequestDto hsmRequest) {
    return submit(hsmRequest);
  }

  @Override
  public ResponseEntity<AsyncHsmResponseDto> signAsync(HsmRequestDto hsmRequest) {
    return submit(hsmRequest);
  }

  @Override
  public ResponseEntity<AsyncHsmResponseDto> getHsmRequest(String correlationId) {
    var asyncResponse = hsmService.getAsyncResult(correlationId);

    return asyncResponseEntity(asyncResponse);
  }

  private ResponseEntity<AsyncHsmResponseDto> submit(HsmRequestDto hsmRequest) {
    var asyncResponse = hsmService.submitAsync(mapper.toDomain(hsmRequest));

    return asyncResponseEntity(asyncResponse);
  }

  private ResponseEntity<AsyncHsmResponseDto> asyncResponseEntity(
      AsyncHsmOperationResult response) {
    var status = "pending".equalsIgnoreCase(response.status())
        ? HttpStatus.ACCEPTED
        : HttpStatus.OK;
    return ResponseEntity.status(status).body(mapper.toResponse(response));
  }
}
