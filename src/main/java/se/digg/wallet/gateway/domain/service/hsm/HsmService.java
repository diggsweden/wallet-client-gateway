// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.hsm;

import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.application.model.hsm.HsmRequestDto;
import se.digg.wallet.gateway.application.model.hsm.HsmResponseDto;
import se.digg.wallet.gateway.application.model.hsm.RegisterStateRequestDto;
import se.digg.wallet.gateway.application.model.hsm.RegisterStateResponseDto;
import se.digg.wallet.gateway.domain.ports.outbound.HsmPort;

@Service
public class HsmService {

  private final HsmPort hsmPort;

  HsmService(HsmPort hsmPort) {
    this.hsmPort = hsmPort;
  }


  public RegisterStateResponseDto registerState(RegisterStateRequestDto request) {
    return hsmPort.registerState(request);
  }


  public HsmResponseDto registerPin(HsmRequestDto request) {
    return hsmPort.registerPin(request);
  }


  public HsmResponseDto changePin(HsmRequestDto request) {
    return hsmPort.changePin(request);
  }


  public HsmResponseDto createSession(HsmRequestDto request) {
    return hsmPort.createSession(request);
  }


  public HsmResponseDto createKey(HsmRequestDto request) {
    return hsmPort.createKey(request);
  }


  public HsmResponseDto listKeys(HsmRequestDto request) {
    return hsmPort.listKeys(request);
  }


  public HsmResponseDto deleteKey(HsmRequestDto request) {
    return hsmPort.deleteKey(request);
  }


  public HsmResponseDto sign(HsmRequestDto request) {
    return hsmPort.sign(request);
  }

}
