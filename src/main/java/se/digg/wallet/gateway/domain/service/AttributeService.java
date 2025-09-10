// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.model.AttributeDto;
import se.digg.wallet.gateway.application.model.CreateAttributeDto;

@Service
public class AttributeService {

  private final WebClient webClient;
  private final ApplicationConfig applicationConfig;

  public AttributeService(WebClient webClient, ApplicationConfig applicationConfig) {
    this.webClient = webClient;
    this.applicationConfig = applicationConfig;
  }

  public AttributeDto createAttribute(CreateAttributeDto createAttributeDto) {
    return webClient
        .post()
        .uri(applicationConfig.downstreamServiceUrl())
        .bodyValue(createAttributeDto)
        .retrieve()
        .bodyToMono(AttributeDto.class)
        .block();
  }

  public AttributeDto getAttribute(String id) {
    return webClient
        .get()
        .uri(applicationConfig.downstreamServiceUrl() + "/" + id)
        .retrieve()
        .bodyToMono(AttributeDto.class)
        .block();
  }
}
