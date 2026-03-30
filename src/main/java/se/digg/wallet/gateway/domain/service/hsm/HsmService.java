// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.hsm;

import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.application.model.hsm.HsmRequestDto;
import se.digg.wallet.gateway.application.model.hsm.HsmResponseDto;
import se.digg.wallet.gateway.application.model.hsm.RegisterStateRequestDto;
import se.digg.wallet.gateway.application.model.hsm.RegisterStateResponseDto;
import se.digg.wallet.gateway.domain.port.in.HsmUseCase;
import se.digg.wallet.gateway.domain.port.out.HsmPort;

@Service
public class HsmService implements HsmUseCase {

  private final HsmPort hsmPort;

  HsmService(HsmPort hsmPort) {
    this.hsmPort = hsmPort;
  }

  @Override
  public RegisterStateResponseDto registerState(String accountId, RegisterStateRequestDto request) {
    return hsmPort.registerState(accountId, request);
  }

  @Override
  public HsmResponseDto registerPin(String accountId, HsmRequestDto request) {
    return hsmPort.registerPin(accountId, request);
  }

  @Override
  public HsmResponseDto changePin(String accountId, HsmRequestDto request) {
    return hsmPort.changePin(accountId, request);
  }

  @Override
  public HsmResponseDto createSession(String accountId, HsmRequestDto request) {
    return hsmPort.createSession(accountId, request);
  }

  @Override
  public HsmResponseDto createKey(String accountId, HsmRequestDto request) {
    return hsmPort.createKey(accountId, request);
  }

  @Override
  public HsmResponseDto listKeys(String accountId, HsmRequestDto request) {
    return hsmPort.listKeys(accountId, request);
  }

  @Override
  public HsmResponseDto deleteKey(String accountId, HsmRequestDto request) {
    return hsmPort.deleteKey(accountId, request);
  }

  @Override
  public HsmResponseDto sign(String accountId, HsmRequestDto request) {
    return hsmPort.sign(accountId, request);
  }

}
