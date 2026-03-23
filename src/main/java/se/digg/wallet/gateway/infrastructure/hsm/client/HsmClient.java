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
import se.digg.wallet.gateway.domain.port.out.HsmPort;

@Component
public class HsmClient implements HsmPort {

    private final RestClient restClient;
    private final String baseUrl;
    private final String postPath;
    private final String newStatePath;

    public HsmClient(RestClient client, ApplicationConfig applicationConfig){
        this.restClient = client.mutate().build();
        this.baseUrl = applicationConfig.walletR2ps().baseurl();
        this.postPath = applicationConfig.walletR2ps().paths().post();
        this.newStatePath = applicationConfig.walletR2ps().paths().newState();
    }

  @Override
  public void registerState(HsmRequestDto request) {
      // TODO do we need a return here?
      restClient
          .post()
          .uri(baseUrl + newStatePath)
          .body(request)
          .contentType(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(HsmRequestDto.class);
  }

  @Override
  public void registerPin(HsmRequestDto request) {
      postRequest(request);
  }

  @Override
  public void changePin(HsmRequestDto request) {
      postRequest(request);
  }

  @Override
  public HsmResponseDto createSession(HsmRequestDto request) {
      return postRequest(request);
  }

  @Override
  public HsmResponseDto createKey(HsmRequestDto request) {
      return postRequest(request);
  }

  @Override
  public HsmResponseDto listKeys(HsmRequestDto request) {
      return postRequest(request);
  }

  @Override
  public void deleteKey(HsmRequestDto request) {
      postRequest(request);
  }

  @Override
  public HsmResponseDto sign(HsmRequestDto request) {
      return postRequest(request);
  }

  private HsmResponseDto postRequest(HsmRequestDto request){
      return restClient
          .post()
          .uri(baseUrl + postPath)
          .body(request)
          .contentType(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(HsmResponseDto.class);
  }

}
