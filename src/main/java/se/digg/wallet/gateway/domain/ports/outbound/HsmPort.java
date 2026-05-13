// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.ports.outbound;

import se.digg.wallet.gateway.domain.model.hsm.AsyncHsmOperationResult;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResult;

public interface HsmPort {

  DeviceStateRegistrationResult registerState(DeviceStateRegistration request);

  AsyncHsmOperationResult submitAsync(HsmOperation request);

  AsyncHsmOperationResult getAsyncResult(String correlationId);

  HsmOperationResult registerPin(HsmOperation request);

  HsmOperationResult changePin(HsmOperation request);

  HsmOperationResult createSession(HsmOperation request);

  HsmOperationResult createKey(HsmOperation request);

  HsmOperationResult listKeys(HsmOperation request);

  HsmOperationResult deleteKey(HsmOperation request);

  HsmOperationResult sign(HsmOperation request);

}
