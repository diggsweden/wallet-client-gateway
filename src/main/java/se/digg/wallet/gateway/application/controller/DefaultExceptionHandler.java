// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import se.digg.wallet.gateway.application.exception.InputValidationException;
import se.digg.wallet.gateway.application.exception.WuaNotFoundException;
import se.digg.wallet.gateway.application.model.BadRequestDto;

@ControllerAdvice
public class DefaultExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionHandler.class);

  
  private final HttpServletRequest httpServletRequest;

  DefaultExceptionHandler(HttpServletRequest httpServletRequest) {
    this.httpServletRequest = httpServletRequest;
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Throwable.class)
  public void handleAnyException(Throwable e) {
    LOGGER.warn("Uncaught exception, responding with 500", e);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND) // 404 for missing resources
  @ExceptionHandler(WuaNotFoundException.class)
  public void handleWuaNotFoundException() {}

  @ExceptionHandler(InputValidationException.class)
  public ResponseEntity<BadRequestDto> handleInputValidationException(InputValidationException e) {
    var instance = httpServletRequest.getServletPath();
    var body = new BadRequestDto(null, e.title().description(), 400, e.detail(), instance);
    return ResponseEntity.badRequest().body(body);
  }
}
