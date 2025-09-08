// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.model.AttributeDto;
import se.digg.wallet.gateway.application.model.CreateAttributeDto;
import se.digg.wallet.gateway.domain.service.AttributeService;

@RestController
@RequestMapping("/attributes")
public class AttributeController {

  private final AttributeService attributeService;

  public AttributeController(AttributeService attributeService) {
    this.attributeService = attributeService;
  }

  @PostMapping
  public ResponseEntity<AttributeDto> createAttribute(CreateAttributeDto createAttributeDto) {
    AttributeDto attributeDto = attributeService.createAttribute(createAttributeDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(attributeDto);
  }

  @GetMapping("/{id}")
  public AttributeDto getAttribute(@PathVariable String id) {
    return attributeService.getAttribute(id);
  }
}
