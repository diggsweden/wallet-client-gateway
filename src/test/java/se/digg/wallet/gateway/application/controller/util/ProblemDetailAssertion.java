// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.util;

import jakarta.annotation.Nullable;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import se.digg.wallet.gateway.api.v0.model.ProblemParameterResponse;
import se.digg.wallet.gateway.api.v0.model.ProblemResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class ProblemDetailAssertion {

  public static void assertProblemDetails(ProblemResponse problemResponse,
      HttpStatus expectedHttpStatus, String transactionId) {

    assertProblemDetails(problemResponse, expectedHttpStatus, transactionId, null, null);
  }

  public static void assertProblemDetails(ProblemResponse problemResponse,
      HttpStatus expectedHttpStatus,
      String transactionId,
      @Nullable String expectedType,
      @Nullable String expectedInvalidParameterProperty) {

    assertThat(problemResponse).isNotNull();
    assertThat(problemResponse.getStatus()).isEqualTo(expectedHttpStatus.value());
    assertThat(problemResponse.getTitle()).isNotEmpty();
    assertThat(problemResponse.getDetail()).isNotNull();
    assertThat(problemResponse.getInstance()).isNotNull();
    assertThat(problemResponse.getType()).isNotNull();
    assertThat(problemResponse.getTransactionId()).isNotEmpty().isEqualTo(transactionId);
    if (expectedType != null) {
      assertThat(problemResponse.getType()).isEqualTo(expectedType);
    }

    if (expectedInvalidParameterProperty != null) {
      assertThat(problemResponse.getInvalidParameters()).isNotEmpty();
      assertThat(expectedInvalidParameterProperty).isIn(problemResponse.getInvalidParameters()
          .stream()
          .map(ProblemParameterResponse::getProperty)
          .filter(Objects::nonNull)
          .toList());
    }
  }
}
