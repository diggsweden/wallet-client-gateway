// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import org.springframework.stereotype.Service;

import se.digg.wallet.gateway.application.model.AttributeDto;
import se.digg.wallet.gateway.application.model.CreateAttributeDto;
import se.digg.wallet.gateway.infrastructure.downstream.client.DownstreamServiceClient;
import se.digg.wallet.gateway.infrastructure.downstream.model.DownstreamCreateAttributeDto;

@Service
public class AttributeService {

  private final DownstreamServiceClient downstreamServiceClient;

  public AttributeService(DownstreamServiceClient downstreamServiceClient) {
    this.downstreamServiceClient = downstreamServiceClient;
  }

  public AttributeDto createAttribute(CreateAttributeDto createAttributeDto) {
    var mapped = new DownstreamCreateAttributeDto(createAttributeDto.value());
    var result = downstreamServiceClient.createAttribute(mapped);
    return new AttributeDto(result.id(), result.value());
  }

  public AttributeDto getAttribute(String id) {
    var result = downstreamServiceClient.getAttribute(id);
    return new AttributeDto(result.id(), result.value());
  }
}
