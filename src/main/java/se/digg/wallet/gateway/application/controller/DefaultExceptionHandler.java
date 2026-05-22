// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static se.digg.wallet.gateway.application.controller.ProblemType.REQUEST_VALIDATION_FAILURE;
import static se.digg.wallet.gateway.application.controller.ProblemType.INTERNAL;
import static se.digg.wallet.gateway.application.controller.ProblemType.REQUEST_ARGUMENT_NOT_VALID;
import static se.digg.wallet.gateway.application.filter.LoggingFilter.MDC_TRANSACTION_ID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import se.digg.wallet.gateway.api.v0.model.ProblemParameterResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;

@RestControllerAdvice
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionHandler.class);

  private final HttpServletRequest httpServletRequest;

  DefaultExceptionHandler(HttpServletRequest httpServletRequest) {
    this.httpServletRequest = httpServletRequest;
  }

  /*
   * Handle Constraint Violation Exception Occurs when validation fails on query parameters, path
   * variables, or service layer methods. Managed by a class-level @Validated annotation.
   */
  @ExceptionHandler({ConstraintViolationException.class})
  public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException e,
      WebRequest request) {

    var problemType = REQUEST_ARGUMENT_NOT_VALID;
    var method = httpServletRequest.getMethod();
    var path = httpServletRequest.getServletPath();

    var problemResponse = buildProblemResponse(problemType)
        .detail(e.getLocalizedMessage())
        .instance(path);

    try {
      var violations = e
          .getConstraintViolations().stream().map(violation -> ProblemParameterResponse.builder()
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

    var violations = Map.of(
        "violations", e.getConstraintViolations().stream().map(violation ->
        MessageFormat.format("{0} {1} {2}",
            violation.getRootBeanClass().getName(),
            violation.getPropertyPath().toString(),
            violation.getMessage())).toList());
    LOGGER.debug("Request argument not valid: {} {} {}", path, method, violations);
    return createResponseEntity(problemType.getHttpStatus(), problemResponse.build());
  }

  /*
   * Handle Method Argument Not Valid Exception Occurs when processing the request body, and a field
   * value does not meet validation criteria. Activated on model class fields annotated with @Valid
   * (@NotNull, @NotBlank, @Size etc.)
   */
  @Override
  protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status,
      WebRequest request) {

    var problemType = REQUEST_VALIDATION_FAILURE;
    var method = httpServletRequest.getMethod();
    var path = httpServletRequest.getServletPath();

    var problemResponse = buildProblemResponse(problemType)
        .detail("Request body field value(s) does not validate.")
        .instance(path);

    try {
      var objectErrors = e.getBindingResult().getGlobalErrors().stream()
          .map(error -> ProblemParameterResponse.builder()
              .reason(error.getDefaultMessage())
              .value(null)
              .property(error.getObjectName())
              .build())
          .toList();

      var fieldErrors = e.getBindingResult().getFieldErrors().stream()
          .map(error -> ProblemParameterResponse.builder()
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

    var errors = Map.of(
      "globalErrors", e.getBindingResult().getGlobalErrors().stream()
          .map(ObjectError::getDefaultMessage).toList(),
      "fieldErrors", e.getBindingResult().getFieldErrors().stream()
        .map(FieldError::getDefaultMessage).toList());
    LOGGER.debug("Input validation failure: {} {} {}", path, method, errors);
    return createResponseEntity(problemType.getHttpStatus(), problemResponse.build());
  }

  /*
   * Handle RestClientException. Occurs on remote service call failures.
   */
  @ExceptionHandler(RestClientException.class)
  public ResponseEntity<Object> handleRestClientException(RestClientException e) {

    var problemType = INTERNAL;
    var method = httpServletRequest.getMethod();
    var path = httpServletRequest.getServletPath();
    var problemResponse = buildProblemResponse(problemType)
        .detail("Remote service failure")
        .instance(path)
        .build();

    LOGGER.error("Remote service failure. {} {}", method, path, e);
    return createResponseEntity(problemType.getHttpStatus(), problemResponse);
  }

  /*
   * Handle exception not handled elsewhere.
   */
  @ExceptionHandler(Throwable.class)
  public ResponseEntity<Object> handleAnyException(Throwable e) {

    var problemType = INTERNAL;
    var method = httpServletRequest.getMethod();
    var path = httpServletRequest.getServletPath();
    var problemResponse = buildProblemResponse(problemType)
        .detail(e.getLocalizedMessage())
        .instance(path)
        .build();

    LOGGER.error("Uncaught exception. {} {}", method, path, e);
    return createResponseEntity(problemType.getHttpStatus(), problemResponse);
  }

  /*
   * For exceptions handled by the super class, map to the problem response defined by the API.
   */
  @Override
  protected ResponseEntity<Object> createResponseEntity(@Nullable Object body,
      HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

    var problemDetailResponse = ProblemResponse.builder()
        .status(statusCode.value())
        .instance(request.getContextPath());

    if (body instanceof ProblemDetail problemDetail) {
      problemDetailResponse
          .title(problemDetail.getTitle())
          .detail(problemDetail.getDetail());

    } else {
      var title = statusCode.is4xxClientError() ? HttpStatus.BAD_REQUEST.getReasonPhrase()
          : HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();

      problemDetailResponse
          .title(title)
          .detail("unknown")
          .build();
    }

    return createResponseEntity(statusCode, problemDetailResponse.build());
  }

  private ResponseEntity<Object> createResponseEntity(HttpStatusCode statusCode,
      ProblemResponse problemResponse) {

    problemResponse.setTransactionId(Optional.of(MDC.get(MDC_TRANSACTION_ID)));
    return ResponseEntity.status(statusCode).body(problemResponse);
  }

  private ProblemResponse.Builder buildProblemResponse(ProblemType problemType) {

    return ProblemResponse.builder()
        .type(problemType.getUri().toASCIIString())
        .title(problemType.getTitle())
        .status(problemType.getHttpStatus().value());
  }
}
