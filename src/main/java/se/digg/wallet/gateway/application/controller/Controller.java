// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.model.CreateWuaDto;
import se.digg.wallet.gateway.application.model.WuaDto;
import se.digg.wallet.gateway.domain.service.WuaService;

@RestController
@RequestMapping("/wua")
public class Controller {

  private final WuaService wuaService;

  public Controller(WuaService wuaService) {
    this.wuaService = wuaService;
  }

  @PostMapping
  public ResponseEntity<WuaDto> createWua(
      @RequestBody CreateWuaDto createWuaDto) {
    WuaDto wuaDto = wuaService.createWua(createWuaDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(wuaDto);
  }

}
