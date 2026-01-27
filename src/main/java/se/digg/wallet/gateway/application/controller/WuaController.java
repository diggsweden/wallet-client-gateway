// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;
import se.digg.wallet.gateway.application.controller.old.WuaControllerV2;
import se.digg.wallet.gateway.application.controller.openapi.wua.CreateWuaOpenApiDocumentation;
import se.digg.wallet.gateway.application.model.wua.CreateWuaDtoV2;
import se.digg.wallet.gateway.application.model.wua.CreateWuaDtoV2;
import se.digg.wallet.gateway.application.model.wua.WuaDto;
import se.digg.wallet.gateway.domain.service.wua.WuaService;

@RestController
@RequestMapping("/wua/v3")
public class WuaController {
  private final Logger logger = LoggerFactory.getLogger(WuaController.class);
  private final WuaService wuaService;

  public WuaController(WuaService wuaService) {
    this.wuaService = wuaService;
  }

  @PostMapping()
  @CreateWuaOpenApiDocumentation
  public ResponseEntity<WuaDto> createWua(
      ChallengeResponseAuthentication challengeResponseAuthentication) {
    logger.debug("Received request from account id: {}",
        challengeResponseAuthentication.getAccountId());
    WuaDto wuaDto = wuaService.createWua(challengeResponseAuthentication.getAccountId());
    return ResponseEntity.status(HttpStatus.CREATED).body(wuaDto);
  }

    @PostMapping()
    @CreateWuaOpenApiDocumentation
    public ResponseEntity<WuaDto> createWuaV2(
            @RequestBody @Valid CreateWuaDtoV2 createWuaDto) {
        logger.debug("Received request for wallet {}", createWuaDto.walletId());
        WuaDto wuaDto = wuaService.createWuaV2(createWuaDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(wuaDto);
    }
}
