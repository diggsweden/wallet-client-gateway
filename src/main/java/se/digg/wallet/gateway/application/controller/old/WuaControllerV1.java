// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.old;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.auth.ApiKeyVerifier;
import se.digg.wallet.gateway.application.controller.openapi.wua.CreateWuaOpenApiDocumentation;
import se.digg.wallet.gateway.application.model.wua.CreateWuaDto;
import se.digg.wallet.gateway.application.model.wua.WuaDto;
import se.digg.wallet.gateway.domain.service.wua.WuaService;

@Deprecated(forRemoval = true)
@RestController
@RequestMapping("/wua")
public class WuaControllerV1 {
  private final Logger logger = LoggerFactory.getLogger(WuaControllerV1.class);
  private final WuaService wuaService;
  private final ApiKeyVerifier apiKeyVerifier;


  public WuaControllerV1(WuaService wuaService, ApiKeyVerifier apiKeyVerifier) {
    this.wuaService = wuaService;
    this.apiKeyVerifier = apiKeyVerifier;

  }

  @PostMapping()
  @CreateWuaOpenApiDocumentation
  public ResponseEntity<WuaDto> createWua(
      @RequestBody @Valid CreateWuaDto createWuaDto) {
    apiKeyVerifier.verify();
    logger.debug("Recieved request for wallet {}", createWuaDto.walletId());
    WuaDto wuaDto = wuaService.createWua(createWuaDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(wuaDto);
  }
}
