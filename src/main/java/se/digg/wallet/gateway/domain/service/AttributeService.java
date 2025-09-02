// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.model.AttributeDto;
import se.digg.wallet.gateway.application.model.CreateAttributeDto;

@Service
public class AttributeService {

  private final RestTemplate restTemplate;
  private final ApplicationConfig applicationConfig;

  public AttributeService(RestTemplate restTemplate, ApplicationConfig applicationConfig) {
    this.restTemplate = restTemplate;
    this.applicationConfig = applicationConfig;
  }

  public AttributeDto createAttribute(CreateAttributeDto createAttributeDto) {
    return restTemplate.postForObject(
        applicationConfig.downstreamServiceUrl(), createAttributeDto, AttributeDto.class);
  }

  public AttributeDto getAttribute(String id) {
    return restTemplate.getForObject(
        applicationConfig.downstreamServiceUrl() + "/" + id, AttributeDto.class);
  }
}
