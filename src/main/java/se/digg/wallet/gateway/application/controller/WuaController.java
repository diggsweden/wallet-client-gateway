// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.WalletUnitAttestationApi;
import se.digg.wallet.gateway.api.v0.model.WuaResponse;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;
import se.digg.wallet.gateway.domain.service.wua.WuaService;

@RestController
public class WuaController implements WalletUnitAttestationApi {
  private final Logger logger = LoggerFactory.getLogger(WuaController.class);
  private final WuaService wuaService;

  public WuaController(WuaService wuaService) {
    this.wuaService = wuaService;
  }

  @Override
  public ResponseEntity<WuaResponse> createWua(Optional<String> nonce) {

    var challengeResponseAuthentication = getChallengeResponseAuthentication();

    if (challengeResponseAuthentication == null) {
      logger.warn("Received request with empty challenge response authentication");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    if (nonce.isPresent() && nonce.get().isBlank()) {
      logger.warn("Received request with empty nonce");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    var accountId = challengeResponseAuthentication.getAccountId();
    logger.debug("Received request from account id: {}, nonce: {}",
        accountId, nonce.orElse(""));
    var wuaDto = wuaService.createWua(accountId, nonce.orElse(""));
    var wuaResponse = WuaResponse.builder().jwt(wuaDto.jwt()).build();
    return ResponseEntity.status(HttpStatus.CREATED).body(wuaResponse);
  }

  private static @Nullable ChallengeResponseAuthentication getChallengeResponseAuthentication() {

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    return switch (authentication) {
      case null -> null;
      case ChallengeResponseAuthentication auth -> auth;
      default -> null;
    };
  }
}
