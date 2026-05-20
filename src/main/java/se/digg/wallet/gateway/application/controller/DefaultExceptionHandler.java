// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static se.digg.wallet.gateway.application.controller.ProblemType.CLIENT_GENERIC;
import static se.digg.wallet.gateway.application.controller.ProblemType.FIELD_VALIDATION_FAILURE;
import static se.digg.wallet.gateway.application.controller.ProblemType.GENERIC_INTERNAL;
import static se.digg.wallet.gateway.application.controller.ProblemType.REQUEST_ARGUMENT_NOT_VALID;
import static se.digg.wallet.gateway.application.filter.LoggingFilter.MDC_TRANSACTION_ID;

import jakarta.servlet.http.HttpServletRequest;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.validation.ConstraintViolationException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import se.digg.wallet.gateway.api.v0.model.ProblemParameterResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;

@ControllerAdvice
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionHandler.class);
  private static final String ABOUT_BLANK = "about:blank";

  private final HttpServletRequest httpServletRequest;

  DefaultExceptionHandler(HttpServletRequest httpServletRequest) {
    this.httpServletRequest = httpServletRequest;
  }

  /*
   * Handle any unexpected and undefined exception.
   */
  @ExceptionHandler(Throwable.class)
  public ResponseEntity<ProblemResponse> handleAnyException(Throwable e) {

    var method = httpServletRequest.getMethod();
    var path = httpServletRequest.getServletPath();
    var problemResponse = buildProblemResponse(GENERIC_INTERNAL)
        .detail(e.getLocalizedMessage())
        .instance(path)
        .build();

    LOGGER.error("Uncaught exception. {} {}", path, method, e);
    return ResponseEntity.internalServerError().body(problemResponse);
  }

  /*
   * Handle Constraint Violation Exception
   * Occurs when validation fails on query parameters, path variables, or service layer methods.
   * Managed by a class-level @Validated annotation.
   */
  @ExceptionHandler({ ConstraintViolationException.class })
  public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException e,
    WebRequest request) {

    var method = httpServletRequest.getMethod();
    var path = httpServletRequest.getServletPath();

    var problemResponse = buildProblemResponse(REQUEST_ARGUMENT_NOT_VALID)
        .detail(e.getLocalizedMessage())
        .instance(path)
        .transactionId(MDC.get(MDC_TRANSACTION_ID));

    try {
      var violations = e.getConstraintViolations().stream().map(violation ->
          ProblemParameterResponse.builder()
            .reason(violation.getMessage())
            .value(Optional.ofNullable(violation.getInvalidValue()).map(Object::toString)
              .orElse(null))
            .property(violation.getPropertyPath().toString())
            .build())
          .toList();

      problemResponse.invalidParameters(violations);

    } catch (Throwable ex) {
      LOGGER.warn("Unable to extract invalid parameters from ConstraintViolationException", e);
    }

    LOGGER.debug("Request argument not valid: {} {} {}", path, method, getErrorsMap(e));
    return ResponseEntity.badRequest().body(problemResponse.build());
  }

  /*
   * Handle Missing Servlet Request Parameter Exception
   * Occurs when the request is missing a required parameter.
   */
  @Override
  protected @Nullable ResponseEntity<Object> handleMissingServletRequestParameter(
    MissingServletRequestParameterException e, HttpHeaders headers, HttpStatusCode status,
    WebRequest request) {

    var method = httpServletRequest.getMethod();
    var path = httpServletRequest.getServletPath();
    var query = httpServletRequest.getQueryString();
    var problemResponse = buildProblemResponse(CLIENT_GENERIC)
        .detail(MessageFormat.format("Missing required parameter: {0}", e.getParameterName()))
        .instance(path)
        .build();

    LOGGER.debug("MissingServletRequestParameter exception. {} {} {}", method, path, query, e);
    return ResponseEntity.badRequest().body(problemResponse);
  }

  /*
  * Handle Method Argument Not Valid Exception
  * Occurs when processing the request body, and a field value does not meet validation criteria.
  * Activated on model class fields annotated with @Valid (@NotNull, @NotBlank, @Size etc.)
  */
  @Override
  protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status,
    WebRequest request) {

    var method = httpServletRequest.getMethod();
    var path = httpServletRequest.getServletPath();

    var problemResponse = buildProblemResponse(FIELD_VALIDATION_FAILURE)
      .detail("Request body field value(s) does not validate.")
      .instance(path)
      .transactionId(MDC.get(MDC_TRANSACTION_ID));

    try {
      var objectErrors = e.getBindingResult().getGlobalErrors().stream().map(error ->
          ProblemParameterResponse.builder()
            .reason(error.getDefaultMessage())
            .value(null)
            .property(error.getObjectName())
            .build())
        .toList();

      var fieldErrors = e.getBindingResult().getFieldErrors().stream().map(error ->
          ProblemParameterResponse.builder()
            .reason(error.getDefaultMessage())
            .value(Optional.ofNullable(error.getRejectedValue()).map(Object::toString)
              .orElse(null))
            .property(error.getField())
            .build())
          .toList();

      var allErrors = Stream.of(objectErrors, fieldErrors)
          .flatMap(Collection::stream)
          .toList();

      problemResponse.invalidParameters(allErrors);

    } catch (Throwable ex) {
      LOGGER.warn("Unable to extract invalid parameters from MethodArgumentNotValidException", e);
    }

    LOGGER.debug("Input validation failure: {} {} {}", path, method, getErrorsMap(e));
    return ResponseEntity.badRequest().body(problemResponse.build());
  }

  /*
   * Handle RestClientException.
   * Occurs on remote service call failures.
   */
  @ExceptionHandler(RestClientException.class)
  public ResponseEntity<ProblemResponse> handleRestClientException(Throwable e) {

    var method = httpServletRequest.getMethod();
    var path = httpServletRequest.getServletPath();
    var problemResponse = buildProblemResponse(GENERIC_INTERNAL)
      .detail("Remote service failure")
      .instance(path)
      .build();

    LOGGER.error("Remote service failure. {} {}", path, method, e);
    return ResponseEntity.internalServerError().body(problemResponse);
  }

  private ProblemResponse.Builder buildProblemResponse(ProblemType problemType) {

    return ProblemResponse.builder()
      .type(problemType.getUri().toASCIIString())
      .title(problemType.getTitle())
      .status(problemType.getHttpStatus().value());
  }

  private Map<String, List<String>> getErrorsMap(ConstraintViolationException e) {

    try {
      return Map.of(
        "violations", e.getConstraintViolations().stream().map(violation ->
            MessageFormat.format("{0} {1} {2}",
              violation.getRootBeanClass().getName(),
              violation.getPropertyPath().toString(),
              violation.getMessage())
          ).toList()
      );

    } catch (Throwable ex) {
      return Map.of();
    }
  }

  private Map<String, List<String>> getErrorsMap(MethodArgumentNotValidException e) {

    try {
      return Map.of(
        "globalErrors", e.getBindingResult().getGlobalErrors().stream()
          .map(ObjectError::getDefaultMessage).toList(),
        "fieldErrors", e.getBindingResult().getFieldErrors().stream()
          .map(FieldError::getDefaultMessage).toList()
      );

    } catch (Throwable ex) {
      return Map.of();
    }
  }

  private Map<String, List<String>> getErrorsMap(TypeMismatchException e) {

    try {
      return Map.of(
        "message", List.of(e.getLocalizedMessage()),
        "property", List.of(e.getPropertyName()),
        "value",  List.of(Optional.ofNullable(e.getValue()).map(Object::toString)
          .orElse(null))
      );

    } catch (Throwable ex) {
      return Map.of();
    }
  }

}
