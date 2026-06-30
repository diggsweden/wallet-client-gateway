// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.api.v0.HsmApi;
import se.digg.wallet.gateway.api.v0.model.HsmRequest;
import se.digg.wallet.gateway.api.v0.model.HsmRequestType;
import se.digg.wallet.gateway.api.v0.model.HsmResponse;
import se.digg.wallet.gateway.api.v0.model.RegisterStateRequest;
import se.digg.wallet.gateway.api.v0.model.RegisterStateResponse;
import se.digg.wallet.gateway.application.auth.CurrentAccount;
import se.digg.wallet.gateway.application.mapper.hsm.HsmMapper;
import se.digg.wallet.gateway.domain.service.hsm.HsmService;

@RestController
public class HsmController implements HsmApi {

  private final HsmService hsmService;
  private final HsmMapper mapper;
  private final CurrentAccount currentAccount;

  HsmController(HsmService hsmService, HsmMapper mapper, CurrentAccount currentAccount) {
    this.hsmService = hsmService;
    this.mapper = mapper;
    this.currentAccount = currentAccount;
  }

  @Override
  public ResponseEntity<HsmResponse> createRequest(HsmRequest hsmRequest,
      Optional<HsmRequestType> type) {

    var result = hsmService.submitAsync(mapper.toDomain(hsmRequest), currentAccount.id());
    var hsmResponse = mapper.toHsmResponse(result);

    return switch (hsmResponse.getStatus()) {
      // TODO: consider how to handle ERROR, should it return 200 OK?
      case COMPLETE, ERROR -> ResponseEntity.ok(hsmResponse);
      case PENDING -> ResponseEntity.status(HttpStatus.ACCEPTED).body(hsmResponse);
    };
  }

  @Override
  public ResponseEntity<HsmResponse> getResult(UUID id) {

    var result = hsmService.getAsyncResult(id, currentAccount.id());
    var hsmResponse = mapper.toHsmResponse(result);

    return switch (hsmResponse.getStatus()) {
      // TODO: consider how to handle ERROR, should it return 200 OK?
      case COMPLETE, ERROR -> ResponseEntity.ok(hsmResponse);
      case PENDING -> ResponseEntity.status(HttpStatus.ACCEPTED).body(hsmResponse);
    };
  }

  @Override
  public ResponseEntity<RegisterStateResponse> saveState(
      RegisterStateRequest registerStateRequest) {

    var result =
        hsmService.registerState(mapper.toDomain(registerStateRequest), currentAccount.id());

    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toRegisterStateResponse(result));
  }
}
