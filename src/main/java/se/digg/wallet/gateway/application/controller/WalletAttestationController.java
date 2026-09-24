// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.WalletAttestationApi;
import se.digg.wallet.gateway.api.v0.model.KeyAttestationRequest;
import se.digg.wallet.gateway.api.v0.model.KeyAttestationResponse;
import se.digg.wallet.gateway.api.v0.model.WalletInstanceAttestationRequest;
import se.digg.wallet.gateway.api.v0.model.WalletInstanceAttestationResponse;
import se.digg.wallet.gateway.application.mapper.walletprovider.WalletProviderMapper;
import se.digg.wallet.gateway.domain.service.AttestationService;

@RestController
public class WalletAttestationController implements WalletAttestationApi {

  @Autowired
  private WalletProviderMapper mapper;

  @Autowired
  private AttestationService attestationService;

  @Override
  public ResponseEntity<KeyAttestationResponse> createKeyAttestation(
      KeyAttestationRequest keyAttestationRequest) {

    var keys = mapper.toDomain(keyAttestationRequest.getKeys());
    var nonce = keyAttestationRequest.getNonce().orElse(null);
    var keyAttestation = attestationService.createKeyAttestation(keys, nonce);
    var response = mapper.toResponse(keyAttestation);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Override
  public ResponseEntity<WalletInstanceAttestationResponse> createWalletInstanceAttestation(
      WalletInstanceAttestationRequest walletInstanceAttestationRequest) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}
