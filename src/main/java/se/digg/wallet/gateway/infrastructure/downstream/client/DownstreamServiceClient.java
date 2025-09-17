// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.downstream.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.infrastructure.downstream.model.DownstreamAttributeDto;
import se.digg.wallet.gateway.infrastructure.downstream.model.DownstreamCreateAttributeDto;

@Component
public class DownstreamServiceClient {

  private final RestClient restClient;
  private final ApplicationConfig applicationConfig;

  public DownstreamServiceClient(RestClient restClient, ApplicationConfig applicationConfig) {
    this.restClient = restClient;
    this.applicationConfig = applicationConfig;
  }

  public DownstreamAttributeDto createAttribute(DownstreamCreateAttributeDto createAttributeDto) {
    return restClient
        .post()
        .uri(applicationConfig.downstreamServiceUrl())
        .body(createAttributeDto)
        .retrieve()
        .body(DownstreamAttributeDto.class);
  }

  public DownstreamAttributeDto getAttribute(String id) {
    return restClient
        .get()
        .uri(applicationConfig.downstreamServiceUrl() + "/" + id)
        .retrieve()
        .body(DownstreamAttributeDto.class);
  }
}
