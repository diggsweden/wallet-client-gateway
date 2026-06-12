// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClientResponseException;
import se.digg.wallet.gateway.application.controller.exception.RemoteResourceNotFoundException;
import se.digg.wallet.gateway.client.hsm.v1.api.HandlersApi;
import se.digg.wallet.gateway.client.hsm.v1.model.AsyncResponseDto;
import se.digg.wallet.gateway.client.hsm.v1.model.EcPublicJwk;
import se.digg.wallet.gateway.client.hsm.v1.model.NewStateResponseDto;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationBuilder;
import se.digg.wallet.gateway.domain.model.hsm.EcPublicJwkBuilder;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationBuilder;
import se.digg.wallet.gateway.infrastructure.hsm.mapper.HsmClientMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
public class HsmAdapterTest {

  @MockitoBean
  private HandlersApi hsmApi;

  @Autowired
  private HsmClientMapper mapper;

  @Autowired
  private HsmAdapter hsmAdapter;

  private UUID randomId;

  @BeforeEach
  void beforeEach() {
    randomId = UUID.randomUUID();
  }

  @Test
  void stateRegistered() {

    var devAuthorizationCode = "the-dev-auth-code";
    var serverId = "the.server.id";
    var status = "complete";
    var stateResponse = NewStateResponseDto.builder()
        .clientId(UUID.randomUUID().toString())
        .devAuthorizationCode(devAuthorizationCode)
        .opaqueServerId(serverId)
        .status(status)
        .serverJwsPublicKey(EcPublicJwk.builder()
            .kid("kid")
            .kty("kty")
            .crv("crv")
            .x("x")
            .y("y")
            .build())
        .build();

    when(hsmApi.createState(any())).thenReturn(stateResponse);

    var deviceStateRegistration = DeviceStateRegistrationBuilder.builder()
        .walletKey(EcPublicJwkBuilder.builder()
            .kid("kid")
            .kty("kty")
            .crv("crv")
            .x("x")
            .y("y")
            .build())
        .build();

    assertDoesNotThrow(() -> hsmAdapter.registerState(deviceStateRegistration));
  }

  @Test
  void hsmRequestCreated() {

    var hsmResponse = AsyncResponseDto.builder()
        .correlationId(randomId)
        .status(se.digg.wallet.gateway.client.hsm.v1.model.AsyncResponseStatus.COMPLETE)
        .result("the-result")
        .build();

    when(hsmApi.service(any())).thenReturn(hsmResponse);

    var hsmOperation = HsmOperationBuilder.builder()
        .clientId(UUID.randomUUID().toString())
        .outerRequestJws("the-request")
        .stateJws("the-state")
        .build();

    assertDoesNotThrow(() -> hsmAdapter.submitAsync(hsmOperation));
  }


  @Test
  void asyncResponseRemoteBadRequest() {

    var restClientResponseException = new RestClientResponseException(
        "Mocked exception",
        HttpStatus.BAD_REQUEST,
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        HttpHeaders.EMPTY,
        "problem-detail-response-body".getBytes(StandardCharsets.UTF_8),
        StandardCharsets.UTF_8);

    when(hsmApi.taskResponse(eq(randomId))).thenThrow(restClientResponseException);

    assertThrows(RestClientResponseException.class, () -> hsmAdapter.getAsyncResult(randomId));
  }

  @Test
  void asyncResponseRemoteServerError() {

    var restClientResponseException = new RestClientResponseException(
        "Mocked exception",
        HttpStatus.INTERNAL_SERVER_ERROR,
        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
        HttpHeaders.EMPTY,
        "problem-detail-response-body".getBytes(StandardCharsets.UTF_8),
        StandardCharsets.UTF_8);

    when(hsmApi.taskResponse(eq(randomId))).thenThrow(restClientResponseException);

    assertThrows(RestClientResponseException.class, () -> hsmAdapter.getAsyncResult(randomId));
  }

  @Test
  void asyncResponseNotFound() {

    var restClientResponseException = new RestClientResponseException(
        "Mocked exception",
        HttpStatus.NOT_FOUND,
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        HttpHeaders.EMPTY,
        "response-body".getBytes(StandardCharsets.UTF_8),
        StandardCharsets.UTF_8);

    when(hsmApi.taskResponse(eq(randomId))).thenThrow(restClientResponseException);

    assertThrows(RemoteResourceNotFoundException.class, () -> hsmAdapter.getAsyncResult(randomId));
  }

  @Test
  void asyncResponse() {

    var expectedResult = UUID.randomUUID().toString();
    var hsmResponse = AsyncResponseDto.builder()
        .correlationId(UUID.randomUUID())
        .status(se.digg.wallet.gateway.client.hsm.v1.model.AsyncResponseStatus.COMPLETE)
        .result(expectedResult)
        .build();
    when(hsmApi.taskResponse(eq(randomId))).thenReturn(hsmResponse);

    var result = assertDoesNotThrow(() -> hsmAdapter.getAsyncResult(randomId));

    assertThat(result).isNotNull();
  }
}
