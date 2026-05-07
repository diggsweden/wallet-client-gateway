// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.ports.outbound;

import se.digg.wallet.gateway.domain.model.hsm.HsmRequest;
import se.digg.wallet.gateway.domain.model.hsm.HsmResponse;
import se.digg.wallet.gateway.domain.model.hsm.RegisterStateRequest;
import se.digg.wallet.gateway.domain.model.hsm.RegisterStateResponse;

public interface HsmPort {

  RegisterStateResponse registerState(RegisterStateRequest request);

  HsmResponse registerPin(HsmRequest request);

  HsmResponse changePin(HsmRequest request);

  HsmResponse createSession(HsmRequest request);

  HsmResponse createKey(HsmRequest request);

  HsmResponse listKeys(HsmRequest request);

  HsmResponse deleteKey(HsmRequest request);

  HsmResponse sign(HsmRequest request);

}
