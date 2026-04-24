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


  public RegisterStateResponseDto registerState(String accountId, RegisterStateRequestDto request) {
    return hsmPort.registerState(accountId, request);
  }


  public HsmResponseDto registerPin(String accountId, HsmRequestDto request) {
    return hsmPort.registerPin(accountId, request);
  }


  public HsmResponseDto changePin(String accountId, HsmRequestDto request) {
    return hsmPort.changePin(accountId, request);
  }


  public HsmResponseDto createSession(String accountId, HsmRequestDto request) {
    return hsmPort.createSession(accountId, request);
  }


  public HsmResponseDto createKey(String accountId, HsmRequestDto request) {
    return hsmPort.createKey(accountId, request);
  }


  public HsmResponseDto listKeys(String accountId, HsmRequestDto request) {
    return hsmPort.listKeys(accountId, request);
  }


  public HsmResponseDto deleteKey(String accountId, HsmRequestDto request) {
    return hsmPort.deleteKey(accountId, request);
  }


  public HsmResponseDto sign(String accountId, HsmRequestDto request) {
    return hsmPort.sign(accountId, request);
  }

}
