// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.hsm;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import se.digg.wallet.gateway.api.v0.model.HsmRequest;
import se.digg.wallet.gateway.api.v0.model.HsmRequestStatus;
import se.digg.wallet.gateway.api.v0.model.HsmResponse;
import se.digg.wallet.gateway.api.v0.model.KeyResponse;
import se.digg.wallet.gateway.api.v0.model.RegisterStateRequest;
import se.digg.wallet.gateway.api.v0.model.RegisterStateResponse;
import se.digg.wallet.gateway.application.controller.HsmController;
import se.digg.wallet.gateway.domain.model.hsm.AsyncHsmOperationResult;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.EcPublicJwk;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;

@Component
public class HsmMapper {

  public DeviceStateRegistration toDomain(RegisterStateRequest request) {
    var deviceKeyRequest = request.getDeviceKey();
    var publicKey = new EcPublicJwk(
        deviceKeyRequest.getKty(),
        deviceKeyRequest.getCrv(),
        deviceKeyRequest.getX(),
        deviceKeyRequest.getY(),
        deviceKeyRequest.getKid());
    return new DeviceStateRegistration(publicKey, request.getTtl());
  }

  public HsmOperation toDomain(HsmRequest request) {
    return new HsmOperation(request.getOuterRequestJws(), request.getClientId(),
        request.getStateJws().orElse(null));
  }

  public HsmResponse toHsmResponse(AsyncHsmOperationResult result) {
    return HsmResponse.builder()
        .id(result.correlationId())
        .status(toHsmRequestStatus(result.status()))
        .result(result.result())
        .resultUrl(toGatewayResultUrl(result))
        .stateJws(result.stateJws())
        .build();
  }

  public RegisterStateResponse toRegisterStateResponse(DeviceStateRegistrationResult result) {
    var serverJwsPublicKey = result.serverJwsPublicKey();
    var serverJwsPublicKeyResponse = KeyResponse.builder()
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

  private String toGatewayResultUrl(AsyncHsmOperationResult response) {
    if (response.resultUrl() == null || response.correlationId() == null) {
      return null;
    }

    UriComponents uriComponents = MvcUriComponentsBuilder
        .fromMethodName(HsmController.class, "getResult", response.correlationId())
        .build();
    String path = uriComponents.getPath();
    return ServletUriComponentsBuilder.fromCurrentContextPath()
        // .path(HsmHttpRoutes.ASYNC_RESULT)
        .path(path)
        .build(response.correlationId())
        .toString();
  }

  private HsmRequestStatus toHsmRequestStatus(String status) {
    return switch (status) {
      case "pending" -> HsmRequestStatus.PENDING;
      case "complete" -> HsmRequestStatus.COMPLETE;
      default -> HsmRequestStatus.ERROR;
    };
  }
}
