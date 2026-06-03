// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.ApiInfoApi;
import se.digg.wallet.gateway.api.v0.model.ApiInfoResponse;

@RestController
public class ApiInfoController implements ApiInfoApi {

  private static final String NAME = "Wallet Client Gateway API";
  private static final String STATUS = "alpha";
  private static final String VERSION = "0.0.2";
  private static final LocalDate RELEASE_DATE = LocalDate.of(2026, 6, 3);
  private static final List<String> LINKS = Collections.emptyList();

  @Override
  public ResponseEntity<ApiInfoResponse> getApiInfo() {
    return ResponseEntity.ok().body(ApiInfoResponse.builder()
        .name(NAME)
        .status(STATUS)
        .version(VERSION)
        .releaseDate(RELEASE_DATE)
        .links(LINKS)
        .build());
  }
}
