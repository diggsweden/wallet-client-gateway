// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.hsm;

import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.api.v0.model.HsmRequestDto;
import se.digg.wallet.gateway.api.v0.model.HsmResponseDto;
import se.digg.wallet.gateway.api.v0.model.RegisterStateResponseDto;
import se.digg.wallet.gateway.domain.model.hsm.EcPublicJwk;
import se.digg.wallet.gateway.domain.model.hsm.HsmRequest;
import se.digg.wallet.gateway.domain.model.hsm.HsmResponse;
import se.digg.wallet.gateway.domain.model.hsm.RegisterStateRequest;
import se.digg.wallet.gateway.domain.model.hsm.RegisterStateResponse;

@Component
public class HsmMapper {

  public RegisterStateRequest toDomain(
      se.digg.wallet.gateway.api.v0.model.RegisterStateRequestDto request) {
    var publicKeyRequest = request.getPublicKey();
    var publicKey = new EcPublicJwk(
        publicKeyRequest.getKty(),
        publicKeyRequest.getCrv(),
        publicKeyRequest.getX(),
        publicKeyRequest.getY(),
        publicKeyRequest.getKid().orElse(""));
    return new RegisterStateRequest(publicKey, request.getOverwrite(), request.getTtl());
  }

  public HsmRequest toDomain(HsmRequestDto request) {
    return new HsmRequest(request.getJwt(), request.getClientId());
  }

  public RegisterStateResponseDto toResponse(RegisterStateResponse response) {
    return RegisterStateResponseDto.builder()
        .status(response.status())
        .clientId(response.clientId())
        .devAuthorizationCode(response.devAuthorizationCode())
        .build();
  }

  public HsmResponseDto toResponse(HsmResponse response) {
    return HsmResponseDto.builder().jwt(response.jwt()).build();
  }
}
