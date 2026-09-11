// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm;

import se.digg.wallet.gateway.client.hsm.v1.model.AsyncResponseDto;
import se.digg.wallet.gateway.client.hsm.v1.model.EcPublicJwk;
import se.digg.wallet.gateway.client.hsm.v1.model.NewStateResponseDto;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationBuilder;
import se.digg.wallet.gateway.domain.model.hsm.EcPublicJwkBuilder;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationBuilder;

import java.util.UUID;

public class HsmTestBuilder {

  private static final String CLIENT_ID = "8c82c1e0-3f8a-4ff9-b4a8-c947fa2e54c0";
  private static final UUID REQUEST_ID = UUID.fromString("616adb0e-0b09-4af9-a5a3-0181a69e373b");

  public static NewStateResponseDto.Builder newStateResponseWithDefaults() {
    return NewStateResponseDto.builder()
        .clientId(CLIENT_ID)
        .devAuthorizationCode("the-dev-auth-code")
        .opaqueServerId("the.server.id")
        .status("complete")
        .serverJwsPublicKey(EcPublicJwk.builder()
            .kid("kid")
            .kty("kty")
            .crv("crv")
            .x("x")
            .y("y")
            .build());
  }

  public static DeviceStateRegistrationBuilder deviceStateRegistrationWithDefaults() {
    return DeviceStateRegistrationBuilder.builder()
        .walletKey(EcPublicJwkBuilder.builder()
            .kid("kid")
            .kty("kty")
            .crv("crv")
            .x("x")
            .y("y")
            .build());
  }

  public static HsmOperationBuilder hsmOperationWithDefaults() {
    return HsmOperationBuilder.builder()
        .clientId(CLIENT_ID)
        .outerRequestJws("the-request")
        .stateJws("the-state");
  }

  public static AsyncResponseDto.Builder asyncResponse() {
    return AsyncResponseDto.builder()
        .correlationId(REQUEST_ID)
        .status(se.digg.wallet.gateway.client.hsm.v1.model.AsyncResponseStatus.COMPLETE)
        .result("the-result");
  }
}
