// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.exception;

import static se.digg.wallet.gateway.application.controller.ProblemType.MAINTENANCE;

import java.net.URI;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class UnavailableDueToMaintenanceException extends ErrorResponseException {

  public UnavailableDueToMaintenanceException(String instance) {
    super(MAINTENANCE.getHttpStatus(), problemDetail(instance), null);
  }

  private static ProblemDetail problemDetail(String instance) {
    var problemDetail = ProblemDetail.forStatus(MAINTENANCE.getHttpStatus());
    problemDetail.setType(MAINTENANCE.getUri());
    problemDetail.setTitle(MAINTENANCE.getTitle());
    problemDetail.setDetail(MAINTENANCE.getDescription());
    problemDetail.setInstance(URI.create(instance));
    return problemDetail;
  }
}
