// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DefaultExceptionHandlerTest {

  private static DefaultExceptionHandler exceptionHandler;

  private WebRequest webRequest;

  @BeforeEach
  void setUp() {
    MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    servletRequest.setRequestURI("/test");
    webRequest = new ServletWebRequest(servletRequest);
    exceptionHandler = new DefaultExceptionHandler(servletRequest);
  }

  @AfterEach
  void cleanUp() {
    MDC.clear();
  }

  @ParameterizedTest
  @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "INTERNAL_SERVER_ERROR"})
  void unknownBodyObjectReturnsProblemResponse(HttpStatus httpStatus) {

    var unknownBody = "a-non-problem-detail-body";

    var responseEntity = assertDoesNotThrow(() -> exceptionHandler
        .createResponseEntity(unknownBody, HttpHeaders.EMPTY, httpStatus, webRequest));

    assertThat(responseEntity.getBody()).isInstanceOf(ProblemResponse.class);
    var problemResponse = (ProblemResponse) responseEntity.getBody();
    assertProblemDetails(problemResponse, httpStatus);
  }

  @Test
  void nonExistingMdcTransactionIdReturnsEmptyTransactionId() {

    MDC.clear();
    var problemDetail = ProblemDetail.forStatus(400);

    var responseEntity = assertDoesNotThrow(() -> exceptionHandler
        .createResponseEntity(problemDetail, HttpHeaders.EMPTY, HttpStatus.BAD_REQUEST,
            webRequest));

    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getBody()).isInstanceOf(ProblemResponse.class);
    var problemResponse = (ProblemResponse) responseEntity.getBody();
    assertThat(problemResponse.getTransactionId()).isEmpty();
  }

  @Test
  void existingMdcTransactionIdReturnsTransactionId() {

    var transactionId = "the-transaction-id";
    MDC.clear();
    MDC.put("transactionId", transactionId);
    var problemDetail = ProblemDetail.forStatus(400);

    var responseEntity = assertDoesNotThrow(() -> exceptionHandler
        .createResponseEntity(problemDetail, HttpHeaders.EMPTY, HttpStatus.BAD_REQUEST,
            webRequest));

    assertThat(responseEntity).isNotNull();
    assertThat(responseEntity.getBody()).isInstanceOf(ProblemResponse.class);
    var problemResponse = (ProblemResponse) responseEntity.getBody();

    assertThat(problemResponse.getTransactionId()).isPresent().get().isEqualTo(transactionId);
  }

  private static void assertProblemDetails(ProblemResponse problemResponse,
      HttpStatus expectedHttpStatus) {

    assertThat(problemResponse).isNotNull();
    assertThat(problemResponse.getStatus()).isEqualTo(expectedHttpStatus.value());
    assertThat(problemResponse.getTitle()).isNotEmpty();
    assertThat(problemResponse.getDetail()).isPresent();
    assertThat(problemResponse.getInstance()).isPresent();
    assertThat(problemResponse.getType()).isPresent();
  }
}
