// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.hsm;

import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.domain.model.hsm.HsmRequest;
import se.digg.wallet.gateway.domain.model.hsm.HsmResponse;
import se.digg.wallet.gateway.domain.model.hsm.RegisterStateRequest;
import se.digg.wallet.gateway.domain.model.hsm.RegisterStateResponse;
import se.digg.wallet.gateway.domain.ports.outbound.HsmPort;

@Service
public class HsmService {

  private final HsmPort hsmPort;

  HsmService(HsmPort hsmPort) {
    this.hsmPort = hsmPort;
  }


  public RegisterStateResponse registerState(RegisterStateRequest request) {
    return hsmPort.registerState(request);
  }


  public HsmResponse registerPin(HsmRequest request) {
    return hsmPort.registerPin(request);
  }


  public HsmResponse changePin(HsmRequest request) {
    return hsmPort.changePin(request);
  }


  public HsmResponse createSession(HsmRequest request) {
    return hsmPort.createSession(request);
  }


  public HsmResponse createKey(HsmRequest request) {
    return hsmPort.createKey(request);
  }


  public HsmResponse listKeys(HsmRequest request) {
    return hsmPort.listKeys(request);
  }


  public HsmResponse deleteKey(HsmRequest request) {
    return hsmPort.deleteKey(request);
  }


  public HsmResponse sign(HsmRequest request) {
    return hsmPort.sign(request);
  }

}
