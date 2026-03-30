// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.model.hsm.HsmRequestDto;
import se.digg.wallet.gateway.application.model.hsm.HsmResponseDto;
import se.digg.wallet.gateway.application.model.hsm.RegisterStateRequestDto;
import se.digg.wallet.gateway.application.model.hsm.RegisterStateResponseDto;
import se.digg.wallet.gateway.domain.port.out.HsmPort;

@Component
public class HsmClient implements HsmPort {

  private final RestClient restClient;
  private final String baseUrl;
  private final String postPath;
  private final String newStatePath;

  HsmClient(RestClient client, ApplicationConfig applicationConfig) {
    this.restClient = client.mutate().build();
    this.baseUrl = applicationConfig.walletR2ps().baseurl();
    this.postPath = applicationConfig.walletR2ps().paths().post();
    this.newStatePath = applicationConfig.walletR2ps().paths().newState();
  }

  @Override
  public RegisterStateResponseDto registerState(String accountId, RegisterStateRequestDto request) {
    var r2psRequest = new R2psNewStateRequestDto(
        request.publicKey(), accountId, request.overwrite(), request.ttl());
    return restClient
        .post()
        .uri(baseUrl + newStatePath)
        .body(r2psRequest)
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(RegisterStateResponseDto.class);
  }

  @Override
  public HsmResponseDto registerPin(String accountId, HsmRequestDto request) {
    return postRequest(accountId, request);
  }

  @Override
  public HsmResponseDto changePin(String accountId, HsmRequestDto request) {
    return postRequest(accountId, request);
  }

  @Override
  public HsmResponseDto createSession(String accountId, HsmRequestDto request) {
    return postRequest(accountId, request);
  }

  @Override
  public HsmResponseDto createKey(String accountId, HsmRequestDto request) {
    return postRequest(accountId, request);
  }

  @Override
  public HsmResponseDto listKeys(String accountId, HsmRequestDto request) {
    return postRequest(accountId, request);
  }

  @Override
  public HsmResponseDto deleteKey(String accountId, HsmRequestDto request) {
    return postRequest(accountId, request);
  }

  @Override
  public HsmResponseDto sign(String accountId, HsmRequestDto request) {
    return postRequest(accountId, request);
  }

  private HsmResponseDto postRequest(String accountId, HsmRequestDto request) {
    String jws = restClient
        .post()
        .uri(baseUrl + postPath)
        .body(new R2psRequestDto(accountId, request.jwt()))
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(String.class);
    return new HsmResponseDto(jws);
  }
}
