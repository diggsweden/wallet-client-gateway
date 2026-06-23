// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.hsm;

import java.util.UUID;
import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelope;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelopes;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistration;
import se.digg.wallet.gateway.domain.model.hsm.DeviceStateRegistrationResult;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperation;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationBuilder;
import se.digg.wallet.gateway.domain.model.hsm.HsmOperationResult;
import se.digg.wallet.gateway.domain.ports.outbound.HsmPort;
import se.digg.wallet.gateway.domain.service.account.AccountService;

@Service
public class HsmService {

  private final HsmPort hsmPort;
  private final AccountService accountService;

  HsmService(HsmPort hsmPort, AccountService accountService) {
    this.hsmPort = hsmPort;
    this.accountService = accountService;
  }

  public DeviceStateRegistrationResult registerState(DeviceStateRegistration request,
      String accountId) {
    DeviceStateRegistrationResult result = hsmPort.registerState(request);

    if (result.clientId() != null) {
      accountService.saveHsmClientId(result.clientId(), accountId);
    }

    persistStateJws(result.stateJws(), accountId);
    return result;
  }

  public HsmOperationResult submitAsync(HsmOperation request, String accountId) {
    HsmOperationResult result = hsmPort.submitAsync(withAccountState(request, accountId));
    persistStateJws(result.stateJws(), accountId);
    return result;
  }

  public HsmOperationResult getAsyncResult(UUID id, String accountId) {
    HsmOperationResult result = hsmPort.getAsyncResult(id);
    persistStateJws(result.stateJws(), accountId);
    return result;
  }

  private HsmOperation withAccountState(HsmOperation request, String accountId) {
    return HsmOperationBuilder.builder()
        .outerRequestJws(request.outerRequestJws())
        .clientId(accountService.getHsmClientId(accountId))
        .stateJws(currentStateJws(accountId))
        .build();
  }

  private String currentStateJws(String accountId) {
    SecurityEnvelopes envelopes = accountService.getSecurityEnvelopes(accountId);
    if (envelopes == null || envelopes.items() == null || envelopes.items().isEmpty()) {
      return null;
    }
    return envelopes.items().getFirst().content();
  }

  private void persistStateJws(String stateJws, String accountId) {
    if (stateJws == null) {
      return;
    }
    accountService.addAccountSecurityEnvelope(new SecurityEnvelope(stateJws), accountId);
  }
}
