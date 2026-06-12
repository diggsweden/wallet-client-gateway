// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.client;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import se.digg.wallet.gateway.application.controller.exception.RemoteResourceNotFoundException;
import se.digg.wallet.gateway.client.hsm.v1.api.HandlersApi;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResult;
import se.digg.wallet.gateway.domain.ports.outbound.HsmPort;
import se.digg.wallet.gateway.infrastructure.hsm.mapper.HsmClientMapper;

@Component
public class HsmAdapter implements HsmPort {

  private final Logger logger = LoggerFactory.getLogger(HsmAdapter.class);

  @Autowired
  private HandlersApi hsmApi;

  @Autowired
  private HsmClientMapper mapper;

  @Override
  public DeviceStateRegistrationResult registerState(DeviceStateRegistration request) {
    var clientRequest = mapper.toClientRequest(request);
    logger.info("Create State client request: {}", clientRequest);
    var result = hsmApi.createState(clientRequest);
    return mapper.toDomainResponse(result);
  }

  @Override
  public HsmOperationResult submitAsync(HsmOperation request) {
    var result = hsmApi.service(mapper.toClientRequest(request));
    return mapper.toDomainResponse(result);
  }

  @Override
  public HsmOperationResult getAsyncResult(UUID id) {
    try {
      logger.info("Find HSM result in remote service. id: {} in", id);
      var result = hsmApi.taskResponse(id);
      logger.info("HSM result from remote service: {}", result);
      return mapper.toDomainResponse(result);

    } catch (RestClientResponseException e) {
      if (HttpStatus.NOT_FOUND.value() == e.getStatusCode().value()) {
        throw new RemoteResourceNotFoundException(
            "HSM Request entry %s was not found in remote service.".formatted(id));
      } else {
        throw e;
      }
    }
  }
}
