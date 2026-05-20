// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.net.URI;
import org.springframework.http.HttpStatus;

public enum ProblemType {

  CLIENT_GENERIC(
    BAD_REQUEST,
    BAD_REQUEST.getReasonPhrase(),
    URI.create("about:blank")
  ),
  REQUEST_UNAUTHORIZED(
    UNAUTHORIZED,
    UNAUTHORIZED.getReasonPhrase(),
    URI.create("about:blank")
  ),
  REQUEST_FORBIDDEN(
    FORBIDDEN,
    FORBIDDEN.getReasonPhrase(),
    URI.create("about:blank")
  ),
  RESOURCE_NOT_FOUND(
    NOT_FOUND,
    NOT_FOUND.getReasonPhrase(),
    URI.create("about:blank")
  ),
  REQUEST_ARGUMENT_NOT_VALID(
    BAD_REQUEST,
    "Request argument not valid",
    URI.create("/problem-details/request-argument-not-valid")
  ),
  FIELD_VALIDATION_FAILURE(
    BAD_REQUEST,
    "Field value not valid",
    URI.create("/problem-details/field-validation-failure")
  ),
  DEVICE_KEY_DUPLICATE(
    BAD_REQUEST,
    "Device Key Duplicate",
    URI.create("/problem-details/device-key-duplicate")
  ),
  DEVICE_KEY_INVALID(
    BAD_REQUEST,
    "Device Key Invalid",
    URI.create("/problem-details/device-key-invalid")
  ),
  GENERIC_INTERNAL(
    INTERNAL_SERVER_ERROR,
    INTERNAL_SERVER_ERROR.getReasonPhrase(),
    URI.create("about:blank")
  );

  private final HttpStatus httpStatus;
  private final String title;
  private final URI uri;

  ProblemType(HttpStatus httpStatus, String title, URI uri) {
    this.httpStatus = httpStatus;
    this.title = title;
    this.uri = uri;
  }

  public HttpStatus getHttpStatus() {
    return this.httpStatus;
  }

  public String getTitle() {
    return this.title;
  }

  public URI getUri() {
    return this.uri;
  }
}
