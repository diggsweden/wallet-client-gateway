// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.ports.outbound;

import java.util.UUID;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResult;

public interface HsmPort {

  DeviceStateRegistrationResult registerState(DeviceStateRegistration request);

  HsmOperationResult submitAsync(HsmOperation request);

  HsmOperationResult getAsyncResult(UUID id);
}
