// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.domain.model.hsm.AsyncHsmOperationResult;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResult;
import se.digg.wallet.gateway.domain.ports.outbound.HsmPort;
import se.digg.wallet.gateway.infrastructure.hsm.mapper.HsmClientMapper;
import se.digg.wallet.gateway.infrastructure.hsm.model.R2PSAsyncOperationResponseDto;

@Component
public class HsmClient implements HsmPort {

  private final RestClient restClient;
  private final String baseUrl;
  private final String syncOperationPath;
  private final String asyncRequestPath;
  private final String asyncPollPath;
  private final String newStatePath;
  private final HsmClientMapper mapper;

  HsmClient(RestClient client, ApplicationConfig applicationConfig, HsmClientMapper mapper) {
    this.restClient = client.mutate().build();
    this.baseUrl = applicationConfig.walletR2ps().baseurl();
    this.syncOperationPath = applicationConfig.walletR2ps().paths().syncOperation();
    this.asyncRequestPath = applicationConfig.walletR2ps().paths().asyncRequest();
    this.asyncPollPath = applicationConfig.walletR2ps().paths().asyncPoll();
    this.newStatePath = applicationConfig.walletR2ps().paths().newState();
    this.mapper = mapper;
  }

  @Override
  public DeviceStateRegistrationResult registerState(DeviceStateRegistration request) {
    var response = restClient
        .post()
        .uri(baseUrl + newStatePath)
        .body(mapper.toClientRequest(request))
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(DeviceStateRegistrationResult.class);
    return mapper.toDomainResponse(response);
  }

  @Override
  public AsyncHsmOperationResult submitAsync(HsmOperation request) {
    var response = restClient
        .post()
        .uri(baseUrl + asyncRequestPath)
        .body(mapper.toClientRequest(request))
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(R2PSAsyncOperationResponseDto.class);
    return mapper.toDomainResponse(response);
  }

  @Override
  public AsyncHsmOperationResult getAsyncResult(String correlationId) {
    var response = restClient
        .get()
        .uri(baseUrl + asyncPollPath.replace("{correlationId}", correlationId))
        .retrieve()
        .body(R2PSAsyncOperationResponseDto.class);
    return mapper.toDomainResponse(response);
  }

  @Override
  public HsmOperationResult registerPin(HsmOperation request) {
    return postRequest(request);
  }

  @Override
  public HsmOperationResult changePin(HsmOperation request) {
    return postRequest(request);
  }

  @Override
  public HsmOperationResult createSession(HsmOperation request) {
    return postRequest(request);
  }

  @Override
  public HsmOperationResult createKey(HsmOperation request) {
    return postRequest(request);
  }

  @Override
  public HsmOperationResult listKeys(HsmOperation request) {
    return postRequest(request);
  }

  @Override
  public HsmOperationResult deleteKey(HsmOperation request) {
    return postRequest(request);
  }

  @Override
  public HsmOperationResult sign(HsmOperation request) {
    return postRequest(request);
  }

  private HsmOperationResult postRequest(HsmOperation request) {
    String jws = restClient
        .post()
        .uri(baseUrl + syncOperationPath)
        .body(mapper.toClientRequest(request))
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(String.class);
    return mapper.toDomainResponse(jws);
  }
}
