// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.WalletProviderApi;
import se.digg.wallet.gateway.api.v0.model.KeyAttestationRequest;
import se.digg.wallet.gateway.api.v0.model.KeyAttestationResponse;
import se.digg.wallet.gateway.application.mapper.walletprovider.WalletProviderMapper;
import se.digg.wallet.gateway.domain.service.WalletProviderService;

@RestController
public class WalletProviderController implements WalletProviderApi {

  @Autowired
  private WalletProviderMapper mapper;

  @Autowired
  private WalletProviderService walletProviderService;

  @Override
  public ResponseEntity<KeyAttestationResponse> createKeyAttestation(
      KeyAttestationRequest keyAttestationRequest) {

    var keys = mapper.toDomain(keyAttestationRequest.getKeys());
    var nonce = keyAttestationRequest.getNonce().orElse(null);
    var keyAttestation = walletProviderService.createKeyAttestation(keys, nonce);
    var response = mapper.toResponse(keyAttestation);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
