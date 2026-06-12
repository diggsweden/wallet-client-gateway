// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.mapper;

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import se.digg.wallet.gateway.client.hsm.v1.model.AsyncResponseDto;
import se.digg.wallet.gateway.client.hsm.v1.model.BffRequest;
import se.digg.wallet.gateway.client.hsm.v1.model.EcPublicJwk;
import se.digg.wallet.gateway.client.hsm.v1.model.NewStateRequestDto;
import se.digg.wallet.gateway.client.hsm.v1.model.NewStateResponseDto;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResultBuilder;
import se.digg.wallet.gateway.domain.model.hsm.HsmAsyncStatus;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResult;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResultBuilder;

@Component
public class HsmClientMapper {

  public NewStateRequestDto toClientRequest(DeviceStateRegistration request) {
    var publicKey = request.walletKey();
    Assert.notNull(publicKey, "WalletKey must not be null");

    var clientPublicKey = EcPublicJwk.builder()
        .kid(publicKey.kid())
        .kty(publicKey.kty())
        .crv(publicKey.crv())
        .x(publicKey.x())
        .y(publicKey.y())
        .build();
    return NewStateRequestDto.builder()
        .publicKey(clientPublicKey)
        .overwrite(true)
        .ttl(request.ttl().orElse(null))
        .build();
  }

  public BffRequest toClientRequest(HsmOperation request) {
    return BffRequest.builder()
        .clientId(request.clientId())
        .outerRequestJws(request.outerRequestJws())
        .stateJws(request.stateJws())
        .build();
  }

  public DeviceStateRegistrationResult toDomainResponse(NewStateResponseDto response) {
    var responseKey = response.getServerJwsPublicKey();

    var publicKey = Optional.ofNullable(responseKey)
        .map(key -> se.digg.wallet.gateway.domain.model.hsm.EcPublicJwkBuilder.builder()
            .kty(key.getKty())
            .crv(key.getCrv())
            .x(key.getX())
            .y(key.getY())
            .kid(key.getKid())
            .build())
        .orElse(null);

    return DeviceStateRegistrationResultBuilder.builder()
        .status(response.getStatus())
        .clientId(response.getClientId())
        .devAuthorizationCode(response.getDevAuthorizationCode())
        .serverJwsPublicKey(publicKey)
        .opaqueServerId(response.getOpaqueServerId())
        .stateJws(response.getStateJws())
        .build();
  }

  public HsmOperationResult toDomainResponse(AsyncResponseDto response) {
    return HsmOperationResultBuilder.builder()
        .id(response.getCorrelationId())
        .status(HsmAsyncStatus.valueOf(response.getStatus().name()))
        .result(response.getResult())
        .resultUrl(response.getResultUrl())
        .stateJws(response.getStateJws())
        .build();
  }
}
