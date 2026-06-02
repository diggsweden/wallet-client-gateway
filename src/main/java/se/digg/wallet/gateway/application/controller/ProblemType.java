// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.net.URI;
import org.springframework.http.HttpStatus;

public enum ProblemType {

  REQUEST_ARGUMENT_NOT_VALID(
      BAD_REQUEST,
      "Request argument not valid",
      URI.create("/problem-details/request-argument-not-valid"),
      "Validation fails on requested header-/query parameters or path variables."),

  REQUEST_VALIDATION_FAILURE(
      BAD_REQUEST,
      "Field value not valid",
      URI.create("/problem-details/field-validation-failure"),
      "Validation fails when processing the request body."),

  RESOURCE_NOT_FOUND(
      NOT_FOUND,
      "Not found",
      URI.create("/problem-details/resource-not-found"),
      "The requested resource could not be found."),

  INTERNAL(
      INTERNAL_SERVER_ERROR,
      INTERNAL_SERVER_ERROR.getReasonPhrase(),
      URI.create("about:blank"),
      "");

  private final HttpStatus httpStatus;
  private final String title;
  private final URI uri;
  private final String description;

  ProblemType(HttpStatus httpStatus, String title, URI uri, String description) {
    this.httpStatus = httpStatus;
    this.title = title;
    this.uri = uri;
    this.description = description;
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

  public String getDescription() {
    return this.description;
  }
}
