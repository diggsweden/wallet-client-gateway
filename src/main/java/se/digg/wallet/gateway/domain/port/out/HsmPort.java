// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.port.out;

import se.digg.wallet.gateway.application.model.hsm.HsmRequestDto;
import se.digg.wallet.gateway.application.model.hsm.HsmResponseDto;
import se.digg.wallet.gateway.application.model.hsm.RegisterStateRequestDto;
import se.digg.wallet.gateway.application.model.hsm.RegisterStateResponseDto;

public interface HsmPort {

  RegisterStateResponseDto registerState(RegisterStateRequestDto request);

  void registerPin(HsmRequestDto request);

  void changePin(HsmRequestDto request);

  HsmResponseDto createSession(HsmRequestDto request);

  HsmResponseDto createKey(HsmRequestDto request);

  HsmResponseDto listKeys(HsmRequestDto request);

  void deleteKey(HsmRequestDto request);

  HsmResponseDto sign(HsmRequestDto request);

}
