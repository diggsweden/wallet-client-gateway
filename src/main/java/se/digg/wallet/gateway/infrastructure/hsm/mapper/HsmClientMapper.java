// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.mapper;

import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.domain.model.hsm.HsmRequest;
import se.digg.wallet.gateway.domain.model.hsm.HsmResponse;
import se.digg.wallet.gateway.domain.model.hsm.RegisterStateRequest;
import se.digg.wallet.gateway.domain.model.hsm.RegisterStateResponse;
import se.digg.wallet.gateway.infrastructure.hsm.client.R2psNewStateRequestDto;
import se.digg.wallet.gateway.infrastructure.hsm.client.R2psRequestDto;

@Component
public class HsmClientMapper {

  public R2psNewStateRequestDto toClientRequest(RegisterStateRequest request) {
    var publicKey = request.publicKey();
    var clientPublicKey = new R2psNewStateRequestDto.EcPublicJwkDto(
        publicKey.kty(),
        publicKey.crv(),
        publicKey.x(),
        publicKey.y(),
        publicKey.kid());
    return new R2psNewStateRequestDto(clientPublicKey, request.overwrite(), request.ttl());
  }

  public R2psRequestDto toClientRequest(HsmRequest request) {
    return new R2psRequestDto(request.clientId(), request.jwt());
  }

  public HsmResponse toDomainResponse(String jwt) {
    return new HsmResponse(jwt);
  }

  public RegisterStateResponse toDomainResponse(RegisterStateResponse response) {
    return response;
  }
}
