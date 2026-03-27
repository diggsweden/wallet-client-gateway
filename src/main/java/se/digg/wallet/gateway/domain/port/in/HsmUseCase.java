// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.port.in;

import se.digg.wallet.gateway.application.model.hsm.HsmRequestDto;
import se.digg.wallet.gateway.application.model.hsm.HsmResponseDto;
import se.digg.wallet.gateway.application.model.hsm.RegisterStateRequestDto;
import se.digg.wallet.gateway.application.model.hsm.RegisterStateResponseDto;

public interface HsmUseCase {

  RegisterStateResponseDto registerState(String accountId, RegisterStateRequestDto request);

  HsmResponseDto registerPin(String accountId, HsmRequestDto request);

  HsmResponseDto changePin(String accountId, HsmRequestDto request);

  HsmResponseDto createSession(String accountId, HsmRequestDto request);

  HsmResponseDto createKey(String accountId, HsmRequestDto request);

  HsmResponseDto listKeys(String accountId, HsmRequestDto request);

  HsmResponseDto deleteKey(String accountId, HsmRequestDto request);

  HsmResponseDto sign(String accountId, HsmRequestDto request);

}
