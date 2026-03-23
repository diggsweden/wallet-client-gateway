// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.port.in;

import se.digg.wallet.gateway.application.model.hsm.HsmRequestDto;
import se.digg.wallet.gateway.application.model.hsm.HsmResponseDto;

public interface HsmUseCase {

  void registerState(String accountId, HsmRequestDto request);

  void registerPin(String accountId, HsmRequestDto request);

  void changePin(String accountId, HsmRequestDto request);

  HsmResponseDto createSession(String accountId, HsmRequestDto request);

  HsmResponseDto createKey(String accountId, HsmRequestDto request);

  HsmResponseDto listKeys(String accountId, HsmRequestDto request);

  void deleteKey(String accountId, HsmRequestDto request);

  HsmResponseDto sign(String accountId, HsmRequestDto request);

}
