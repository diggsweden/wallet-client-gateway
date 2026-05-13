// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.hsm;

import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.domain.model.hsm.AsyncHsmOperationResult;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResult;
import se.digg.wallet.gateway.domain.ports.outbound.HsmPort;

@Service
public class HsmService {

  private final HsmPort hsmPort;

  HsmService(HsmPort hsmPort) {
    this.hsmPort = hsmPort;
  }


  public DeviceStateRegistrationResult registerState(DeviceStateRegistration request) {
    return hsmPort.registerState(request);
  }

  public AsyncHsmOperationResult submitAsync(HsmOperation request) {
    return hsmPort.submitAsync(request);
  }

  public AsyncHsmOperationResult getAsyncResult(String correlationId) {
    return hsmPort.getAsyncResult(correlationId);
  }

  public HsmOperationResult registerPin(HsmOperation request) {
    return hsmPort.registerPin(request);
  }


  public HsmOperationResult changePin(HsmOperation request) {
    return hsmPort.changePin(request);
  }


  public HsmOperationResult createSession(HsmOperation request) {
    return hsmPort.createSession(request);
  }


  public HsmOperationResult createKey(HsmOperation request) {
    return hsmPort.createKey(request);
  }


  public HsmOperationResult listKeys(HsmOperation request) {
    return hsmPort.listKeys(request);
  }


  public HsmOperationResult deleteKey(HsmOperation request) {
    return hsmPort.deleteKey(request);
  }


  public HsmOperationResult sign(HsmOperation request) {
    return hsmPort.sign(request);
  }

}
