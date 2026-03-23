// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.hsm;

import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.application.model.hsm.HsmRequestDto;
import se.digg.wallet.gateway.application.model.hsm.HsmResponseDto;
import se.digg.wallet.gateway.domain.port.in.HsmUseCase;
import se.digg.wallet.gateway.domain.port.out.HsmPort;

@Service
public class HsmService implements HsmUseCase {

  private final HsmPort hsmPort;

   HsmService(HsmPort hsmPort) {
    this.hsmPort = hsmPort;
  }

  @Override
  public void registerState(String accountId, HsmRequestDto request) {
    hsmPort.registerState(request);
  }

  @Override
  public void registerPin(String accountId, HsmRequestDto request) {
    hsmPort.registerPin(request);
  }

  @Override
  public void changePin(String accountId, HsmRequestDto request) {
    hsmPort.changePin(request);
  }

  @Override
  public HsmResponseDto createSession(String accountId, HsmRequestDto request) {
    return hsmPort.createSession(request);
  }

  @Override
  public HsmResponseDto createKey(String accountId, HsmRequestDto request) {
    return hsmPort.createKey(request);
  }

  @Override
  public HsmResponseDto listKeys(String accountId, HsmRequestDto request) {
    return hsmPort.listKeys(request);
  }

  @Override
  public void deleteKey(String accountId, HsmRequestDto request) {
    hsmPort.deleteKey(request);
  }

  @Override
  public HsmResponseDto sign(String accountId, HsmRequestDto request) {
    return hsmPort.sign(request);
  }

}
