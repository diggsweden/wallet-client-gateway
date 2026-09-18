// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.hsm;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import se.digg.wallet.gateway.api.v0.model.EcJwkResponseDto;
import se.digg.wallet.gateway.api.v0.model.HsmAsyncStatusDto;
import se.digg.wallet.gateway.api.v0.model.HsmRequestDto;
import se.digg.wallet.gateway.api.v0.model.HsmResponseDto;
import se.digg.wallet.gateway.api.v0.model.RegisterStateRequestDto;
import se.digg.wallet.gateway.api.v0.model.RegisterStateResponseDto;
import se.digg.wallet.gateway.application.controller.HsmController;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationBuilder;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.EcPublicJwkBuilder;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationBuilder;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResult;

@Component
public class HsmMapper {

  public DeviceStateRegistration toDomain(RegisterStateRequestDto request) {
    var deviceKeyRequest = request.getDeviceKey();
    var publicKey = EcPublicJwkBuilder.builder()
        .kty(deviceKeyRequest.getKty())
        .crv(deviceKeyRequest.getCrv())
        .x(deviceKeyRequest.getX())
        .y(deviceKeyRequest.getY())
        .kid(deviceKeyRequest.getKid())
        .build();
    return DeviceStateRegistrationBuilder.builder()
        .walletKey(publicKey)
        .ttl(request.getTtl())
        .build();
  }

  public HsmOperation toDomain(HsmRequestDto request) {
    return HsmOperationBuilder.builder()
        .outerRequestJws(request.getOuterRequestJws())
        .build();
  }

  public HsmResponseDto toHsmResponse(HsmOperationResult result) {
    return HsmResponseDto.builder()
        .id(result.id())
        .status(HsmAsyncStatusDto.fromValue(result.status().name()))
        .result(result.result())
        .resultUrl(toGatewayResultUrl(result))
        .build();
  }

  public RegisterStateResponseDto toRegisterStateResponse(DeviceStateRegistrationResult result) {
    var serverJwsPublicKey = result.serverJwsPublicKey();
    var serverJwsPublicKeyResponse = EcJwkResponseDto.builder()
        .alg(null)
        .crv(serverJwsPublicKey.crv())
        .kid(serverJwsPublicKey.kid())
        .kty(serverJwsPublicKey.kty())
        .use(null)
        .x(serverJwsPublicKey.x())
        .y(serverJwsPublicKey.y())
        .build();

    return RegisterStateResponseDto.builder()
        .serverJwsPublicKey(serverJwsPublicKeyResponse)
        .status(result.status())
        .opaqueServerId(result.opaqueServerId())
        .devAuthorizationCode(result.devAuthorizationCode())
        .build();
  }

  private String toGatewayResultUrl(HsmOperationResult response) {
    if (response.resultUrl() == null || response.id() == null) {
      return null;
    }

    UriComponents uriComponents = MvcUriComponentsBuilder
        .fromMethodName(HsmController.class, "getResult", response.id())
        .build();
    String path = uriComponents.getPath();
    return ServletUriComponentsBuilder.fromCurrentContextPath()
        // .path(HsmHttpRoutes.ASYNC_RESULT)
        .path(path)
        .build(response.id())
        .toString();
  }
}
