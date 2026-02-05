// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.validation.Valid;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;
import se.digg.wallet.gateway.application.controller.openapi.wua.CreateWuaOpenApiDocumentation;
import se.digg.wallet.gateway.application.model.wua.WuaDto;
import se.digg.wallet.gateway.domain.service.wua.WuaService;

@RestController
@RequestMapping("/wua/v3")
@Validated
public class WuaController {
  private final Logger logger = LoggerFactory.getLogger(WuaController.class);
  private final WuaService wuaService;

  public WuaController(WuaService wuaService) {
    this.wuaService = wuaService;
  }

  @PostMapping()
  @CreateWuaOpenApiDocumentation
  public ResponseEntity<WuaDto> createWua(
      @Valid ChallengeResponseAuthentication challengeResponseAuthentication,
      @RequestParam Optional<String> nonce) {
    if (nonce.isPresent() && nonce.get().isBlank()) {
      logger.warn("Received request with empty nonce");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    logger.debug("Received request from account id: {}, nonce: {}",
        challengeResponseAuthentication.getAccountId(), nonce.orElse(""));
    WuaDto wuaDto = wuaService.createWua(challengeResponseAuthentication.getAccountId(),
        nonce.orElse(""));
    return ResponseEntity.status(HttpStatus.CREATED).body(wuaDto);
  }
}
