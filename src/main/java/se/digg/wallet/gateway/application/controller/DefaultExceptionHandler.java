// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import se.digg.wallet.gateway.application.controller.exception.BadRequestException;
import se.digg.wallet.gateway.application.model.BadRequestDto;

@ControllerAdvice
public class DefaultExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionHandler.class);

  private HttpServletRequest httpServletRequest;

  DefaultExceptionHandler(HttpServletRequest httpServletRequest) {
    this.httpServletRequest = httpServletRequest;
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Throwable.class)
  public void handleAnyException(Throwable e) {
    LOGGER.warn("Uncaught exception, responding with 500", e);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(BAD_REQUEST)
  public ResponseEntity<BadRequestDto> handleInputValidationException(
      MethodArgumentNotValidException e) {

    var instance = httpServletRequest.getServletPath();
    var body = new BadRequestDto(
        null,
        "Validation error",
        HttpStatus.BAD_REQUEST.value(),
        getErrorsMap(e).toString(),
        instance);
    LOGGER.debug("Validation error, not able to parse input {}", getErrorsMap(e));
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(BAD_REQUEST)
  public ResponseEntity<BadRequestDto> handleBadRequestException(
      BadRequestException e) {
    var instance = httpServletRequest.getServletPath();
    var body = new BadRequestDto(
        null,
        "Validation error",
        HttpStatus.BAD_REQUEST.value(),
        e.detail(),
        instance);
    LOGGER.debug("Validation error", e);
    return ResponseEntity.badRequest().body(body);
  }


  private Map<String, List<String>> getErrorsMap(MethodArgumentNotValidException e) {

    Map<String, List<String>> errorResponse = new HashMap<>();
    errorResponse.put("errors", e.getBindingResult().getFieldErrors()
        .stream().map(FieldError::getDefaultMessage).toList());
    return errorResponse;
  }
}


