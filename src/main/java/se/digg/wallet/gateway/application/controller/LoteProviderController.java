// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.TrustApi;
import se.digg.wallet.gateway.api.v0.model.LoteProviderItemResponse;
import se.digg.wallet.gateway.api.v0.model.LoteProvidersResponse;
import se.digg.wallet.gateway.domain.service.LoteProviderService;

@RestController
public class LoteProviderController implements TrustApi {

  @Autowired
  private LoteProviderService loteProviderService;

  @Override
  public ResponseEntity<LoteProvidersResponse> getLoteProviders() {

    var loteProviders = loteProviderService.getLoteProviders();
    var responseItems = loteProviders.stream()
        .map(loteProvider -> LoteProviderItemResponse.builder()
            .id(loteProvider.id())
            .url(loteProvider.url())
            .build())
        .toList();
    var response = LoteProvidersResponse.builder()
        .items(responseItems)
        .build();

    return ResponseEntity.ok(response);
  }
}
