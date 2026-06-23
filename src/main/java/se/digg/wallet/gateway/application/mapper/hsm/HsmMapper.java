// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.hsm;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import se.digg.wallet.gateway.api.v0.model.HsmAsyncStatus;
import se.digg.wallet.gateway.api.v0.model.HsmRequest;
import se.digg.wallet.gateway.api.v0.model.HsmResponse;
import se.digg.wallet.gateway.api.v0.model.EcJwkResponse;
import se.digg.wallet.gateway.api.v0.model.RegisterStateRequest;
import se.digg.wallet.gateway.api.v0.model.RegisterStateResponse;
import se.digg.wallet.gateway.application.controller.HsmController;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResult;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationBuilder;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.EcPublicJwkBuilder;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationBuilder;

@Component
public class HsmMapper {

  public DeviceStateRegistration toDomain(RegisterStateRequest request) {
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

  public HsmOperation toDomain(HsmRequest request) {
    return HsmOperationBuilder.builder()
        .outerRequestJws(request.getOuterRequestJws())
        .build();
  }

  public HsmResponse toHsmResponse(HsmOperationResult result) {
    return HsmResponse.builder()
        .id(result.id())
        .status(HsmAsyncStatus.fromValue(result.status().name()))
        .result(result.result())
        .resultUrl(toGatewayResultUrl(result))
        .stateJws(result.stateJws())
        .build();
  }

  public RegisterStateResponse toRegisterStateResponse(DeviceStateRegistrationResult result) {
    var serverJwsPublicKey = result.serverJwsPublicKey();
    var serverJwsPublicKeyResponse = EcJwkResponse.builder()
        .alg(null)
        .crv(serverJwsPublicKey.crv())
        .kid(serverJwsPublicKey.kid())
        .kty(serverJwsPublicKey.kty())
        .use(null)
        .x(serverJwsPublicKey.x())
        .y(serverJwsPublicKey.y())
        .build();

    return RegisterStateResponse.builder()
        .clientId(result.clientId())
        .serverJwsPublicKey(serverJwsPublicKeyResponse)
        .status(result.status())
        .opaqueServerId(result.opaqueServerId())
        .devAuthorizationCode(result.devAuthorizationCode())
        .stateJws(result.stateJws())
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
