// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.ports.outbound;

import se.digg.wallet.gateway.domain.model.hsm.AsyncHsmOperationResult;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;

public interface HsmPort {

  DeviceStateRegistrationResult registerState(DeviceStateRegistration request);

  AsyncHsmOperationResult submitAsync(HsmOperation request);

  AsyncHsmOperationResult getAsyncResult(String id);
}
