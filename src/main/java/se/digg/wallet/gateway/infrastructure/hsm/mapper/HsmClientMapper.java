// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.mapper;

import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.domain.model.hsm.AsyncHsmOperationError;
import se.digg.wallet.gateway.domain.model.hsm.AsyncHsmOperationResult;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResult;
import se.digg.wallet.gateway.infrastructure.hsm.model.R2PSAsyncOperationResponseDto;
import se.digg.wallet.gateway.infrastructure.hsm.model.R2PSDeviceStateRequestDto;
import se.digg.wallet.gateway.infrastructure.hsm.model.R2PSEcPublicJwkDto;
import se.digg.wallet.gateway.infrastructure.hsm.model.R2PSOperationRequestDto;

@Component
public class HsmClientMapper {

  public R2PSDeviceStateRequestDto toClientRequest(DeviceStateRegistration request) {
    var publicKey = request.walletKey();
    var clientPublicKey = new R2PSEcPublicJwkDto(
        publicKey.kty(),
        publicKey.crv(),
        publicKey.x(),
        publicKey.y(),
        publicKey.kid());
    return new R2PSDeviceStateRequestDto(clientPublicKey, false, request.ttl());
  }

  public R2PSOperationRequestDto toClientRequest(HsmOperation request) {
    return new R2PSOperationRequestDto(request.clientId(), request.outerRequestJws(), request.stateJws());
  }

  public HsmOperationResult toDomainResponse(String jwt) {
    return new HsmOperationResult(jwt);
  }

  public AsyncHsmOperationResult toDomainResponse(R2PSAsyncOperationResponseDto response) {
    var error = response.error() == null
        ? null
        : new AsyncHsmOperationError(response.error().message(), response.error().httpStatus());
    return new AsyncHsmOperationResult(
        response.correlationId(),
        response.status(),
        response.result(),
        response.resultUrl(),
        error,
        response.stateJws());
  }

  public DeviceStateRegistrationResult toDomainResponse(DeviceStateRegistrationResult response) {
    return response;
  }
}
