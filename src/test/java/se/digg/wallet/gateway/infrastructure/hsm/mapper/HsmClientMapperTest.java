// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.mapper;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.digg.wallet.gateway.client.hsm.v1.model.AsyncResponseDto;
import se.digg.wallet.gateway.client.hsm.v1.model.EcPublicJwk;
import se.digg.wallet.gateway.client.hsm.v1.model.NewStateResponseDto;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationBuilder;
import se.digg.wallet.gateway.domain.model.hsm.HsmAsyncStatus;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class HsmClientMapperTest {

  @Autowired
  private HsmClientMapper mapper;

  private UUID randomId;

  @BeforeEach
  void beforeEach() {
    randomId = UUID.randomUUID();
  }

  @Test
  void throwsExceptionWhenMissingWalletKey() {

    var request = DeviceStateRegistrationBuilder.builder()
        .walletKey(null)
        .build();

    assertThrows(IllegalArgumentException.class, () -> mapper.toClientRequest(request));
  }

  @Test
  void mapToDeviceStateRegistrationClientRequest() {

    var ttl = "ttl";
    var kid = "kid";
    var kty = "kty";
    var crv = "crv";
    var x = "x";
    var y = "y";

    var request = DeviceStateRegistrationBuilder.builder()
        .ttl(Optional.of(ttl))
        .walletKey(
            se.digg.wallet.gateway.domain.model.hsm.EcPublicJwkBuilder.builder()
                .kid(kid)
                .kty(kty)
                .crv(crv)
                .x(x)
                .y(y)
                .build())
        .build();

    var result = assertDoesNotThrow(() -> mapper.toClientRequest(request));

    assertThat(result).isNotNull();
    assertThat(result.getTtl()).isEqualTo(ttl);
    assertThat(result.getPublicKey()).isNotNull();
    assertThat(result.getPublicKey().getKid()).isEqualTo(kid);
    assertThat(result.getPublicKey().getKty()).isEqualTo(kty);
    assertThat(result.getPublicKey().getCrv()).isEqualTo(crv);
    assertThat(result.getPublicKey().getX()).isEqualTo(x);
    assertThat(result.getPublicKey().getY()).isEqualTo(y);
    assertThat(result.getOverwrite()).isNotNull();
  }

  @Test
  void mapToDeviceStateRegistrationDomainWithNullPublicKey() {

    var clientId = UUID.randomUUID().toString();
    var opaqueServerId = "opaque.server.id";
    var devAuthorizationCode = "dev-auth-code";
    var status = "status";

    var request = NewStateResponseDto.builder()
        .clientId(clientId)
        .opaqueServerId(opaqueServerId)
        .devAuthorizationCode(devAuthorizationCode)
        .status(status)
        .build();

    var result = assertDoesNotThrow(() -> mapper.toDomainResponse(request));

    assertThat(result).isNotNull();
    assertThat(result.clientId()).isEqualTo(clientId);
    assertThat(result.opaqueServerId()).isEqualTo(opaqueServerId);
    assertThat(result.devAuthorizationCode()).isEqualTo(devAuthorizationCode);
    assertThat(result.status()).isEqualTo(status);
    assertThat(result.serverJwsPublicKey()).isNull();
  }

  @Test
  void mapToDeviceStateRegistrationDomain() {

    var clientId = UUID.randomUUID().toString();
    var opaqueServerId = "opaque.server.id";
    var devAuthorizationCode = "dev-auth-code";
    var status = "status";
    var kid = "kid";
    var kty = "kty";
    var crv = "crv";
    var x = "x";
    var y = "y";

    var request = NewStateResponseDto.builder()
        .clientId(clientId)
        .opaqueServerId(opaqueServerId)
        .devAuthorizationCode(devAuthorizationCode)
        .status(status)
        .serverJwsPublicKey(EcPublicJwk.builder()
            .kid(kid)
            .kty(kty)
            .crv(crv)
            .x(x)
            .y(y)
            .build())
        .build();

    var result = assertDoesNotThrow(() -> mapper.toDomainResponse(request));

    assertThat(result).isNotNull();
    assertThat(result.clientId()).isEqualTo(clientId);
    assertThat(result.opaqueServerId()).isEqualTo(opaqueServerId);
    assertThat(result.devAuthorizationCode()).isEqualTo(devAuthorizationCode);
    assertThat(result.status()).isEqualTo(status);
    assertThat(result.serverJwsPublicKey()).isNotNull();
    assertThat(result.serverJwsPublicKey().kid()).isEqualTo(kid);
    assertThat(result.serverJwsPublicKey().kty()).isEqualTo(kty);
    assertThat(result.serverJwsPublicKey().crv()).isEqualTo(crv);
    assertThat(result.serverJwsPublicKey().x()).isEqualTo(x);
    assertThat(result.serverJwsPublicKey().y()).isEqualTo(y);
  }

  @Test
  void mapToHsmOperationClientRequest() {

    var clientId = UUID.randomUUID().toString();
    var outerRequestJws = "the-outer-request";
    var stateJws = "the-state";
    var request = HsmOperationBuilder.builder()
        .clientId(clientId)
        .outerRequestJws(outerRequestJws)
        .stateJws(stateJws)
        .build();

    var result = assertDoesNotThrow(() -> mapper.toClientRequest(request));

    assertThat(result).isNotNull();
    assertThat(result.getClientId()).isEqualTo(clientId);
    assertThat(result.getOuterRequestJws()).isEqualTo(outerRequestJws);
    assertThat(result.getStateJws()).isEqualTo(stateJws);
  }

  @Test
  void mapToHsmOperationDomain() {

    var resultValue = UUID.randomUUID().toString();
    var resultUrl = "result-url";
    var stateJws = UUID.randomUUID().toString();
    var request = AsyncResponseDto.builder()
        .correlationId(randomId)
        .status(se.digg.wallet.gateway.client.hsm.v1.model.AsyncResponseStatus.COMPLETE)
        .result(resultValue)
        .resultUrl(resultUrl)
        .stateJws(stateJws)
        .build();

    var result = assertDoesNotThrow(() -> mapper.toDomainResponse(request));

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(randomId);
    assertThat(result.status()).isEqualTo(HsmAsyncStatus.COMPLETE);
    assertThat(result.result()).isEqualTo(resultValue);
    assertThat(result.resultUrl()).isEqualTo(resultUrl);
    assertThat(result.stateJws()).isEqualTo(stateJws);
  }
}
