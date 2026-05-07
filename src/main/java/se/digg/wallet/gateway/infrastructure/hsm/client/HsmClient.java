// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.domain.model.hsm.HsmRequest;
import se.digg.wallet.gateway.domain.model.hsm.HsmResponse;
import se.digg.wallet.gateway.domain.model.hsm.RegisterStateRequest;
import se.digg.wallet.gateway.domain.model.hsm.RegisterStateResponse;
import se.digg.wallet.gateway.domain.ports.outbound.HsmPort;
import se.digg.wallet.gateway.infrastructure.hsm.mapper.HsmClientMapper;

@Component
public class HsmClient implements HsmPort {

  private final RestClient restClient;
  private final String baseUrl;
  private final String postPath;
  private final String newStatePath;
  private final HsmClientMapper mapper;

  HsmClient(RestClient client, ApplicationConfig applicationConfig, HsmClientMapper mapper) {
    this.restClient = client.mutate().build();
    this.baseUrl = applicationConfig.walletR2ps().baseurl();
    this.postPath = applicationConfig.walletR2ps().paths().post();
    this.newStatePath = applicationConfig.walletR2ps().paths().newState();
    this.mapper = mapper;
  }

  @Override
  public RegisterStateResponse registerState(RegisterStateRequest request) {
    var response = restClient
        .post()
        .uri(baseUrl + newStatePath)
        .body(mapper.toClientRequest(request))
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(RegisterStateResponse.class);
    return mapper.toDomainResponse(response);
  }

  @Override
  public HsmResponse registerPin(HsmRequest request) {
    return postRequest(request);
  }

  @Override
  public HsmResponse changePin(HsmRequest request) {
    return postRequest(request);
  }

  @Override
  public HsmResponse createSession(HsmRequest request) {
    return postRequest(request);
  }

  @Override
  public HsmResponse createKey(HsmRequest request) {
    return postRequest(request);
  }

  @Override
  public HsmResponse listKeys(HsmRequest request) {
    return postRequest(request);
  }

  @Override
  public HsmResponse deleteKey(HsmRequest request) {
    return postRequest(request);
  }

  @Override
  public HsmResponse sign(HsmRequest request) {
    return postRequest(request);
  }

  private HsmResponse postRequest(HsmRequest request) {
    String jws = restClient
        .post()
        .uri(baseUrl + postPath)
        .body(mapper.toClientRequest(request))
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(String.class);
    return mapper.toDomainResponse(jws);
  }
}
