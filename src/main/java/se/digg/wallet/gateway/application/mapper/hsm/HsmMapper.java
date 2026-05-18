// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.hsm;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import se.digg.wallet.gateway.api.v0.model.AsyncHsmErrorDto;
import se.digg.wallet.gateway.api.v0.model.AsyncHsmResponseDto;
import se.digg.wallet.gateway.api.v0.model.HsmRequestDto;
import se.digg.wallet.gateway.api.v0.model.HsmResponseDto;
import se.digg.wallet.gateway.api.v0.model.RegisterStateRequestDto;
import se.digg.wallet.gateway.api.v0.model.RegisterStateResponseDto;
import se.digg.wallet.gateway.application.controller.HsmHttpRoutes;
import se.digg.wallet.gateway.domain.model.hsm.AsyncHsmOperationResult;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.EcPublicJwk;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResult;

@Component
public class HsmMapper {

  public DeviceStateRegistration toDomain(RegisterStateRequestDto request) {
    var publicKeyRequest = request.getPublicKey();
    var publicKey = new EcPublicJwk(
        publicKeyRequest.getKty(),
        publicKeyRequest.getCrv(),
        publicKeyRequest.getX(),
        publicKeyRequest.getY(),
        publicKeyRequest.getKid());
    return new DeviceStateRegistration(publicKey, request.getOverwrite(), request.getTtl());
  }

  public HsmOperation toDomain(HsmRequestDto request) {
    return new HsmOperation(request.getJwt(), request.getClientId());
  }

  public RegisterStateResponseDto toResponse(DeviceStateRegistrationResult response) {
    return RegisterStateResponseDto.builder()
        .status(response.status())
        .clientId(response.clientId())
        .devAuthorizationCode(response.devAuthorizationCode())
        .serverJwsPublicKey(response.serverJwsPublicKey())
        .opaqueServerId(response.opaqueServerId())
        .build();
  }

  public HsmResponseDto toResponse(HsmOperationResult response) {
    return HsmResponseDto.builder().jwt(response.jwt()).build();
  }

  public AsyncHsmResponseDto toResponse(AsyncHsmOperationResult response) {
    var error = response.error() == null
        ? null
        : AsyncHsmErrorDto.builder()
            .message(response.error().message())
            .httpStatus(response.error().httpStatus())
            .build();
    return AsyncHsmResponseDto.builder()
        .correlationId(response.correlationId())
        .status(response.status())
        .result(response.result())
        .resultUrl(toGatewayResultUrl(response))
        .error(error)
        .build();
  }

  private String toGatewayResultUrl(AsyncHsmOperationResult response) {
    if (response.resultUrl() == null || response.correlationId() == null) {
      return null;
    }

    return ServletUriComponentsBuilder.fromCurrentContextPath()
        .path(HsmHttpRoutes.ASYNC_RESULT)
        .build(response.correlationId())
        .toString();
  }
}
