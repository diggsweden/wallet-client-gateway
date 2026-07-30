// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.account;

import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.api.v0.model.CreateAccountRequest;
import se.digg.wallet.gateway.api.v0.model.CreateAccountResponse;
import se.digg.wallet.gateway.api.v0.model.EcJwkRequest;
import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.model.account.NewAccountBuilder;

@Component
public class AccountMapper {

  public CreateAccountResponse toResponse(Account account) {
    return CreateAccountResponse
        .builder()
        .accountId(account.id())
        .build();
  }

  public NewAccount toDomain(CreateAccountRequest request) {
    return NewAccountBuilder.builder()
        .personalIdentityNumber(request.getPersonalIdentityNumber().orElse(null))
        .email(request.getEmail().orElse(request.getEmailAdress().orElse(null)))
        .phoneNumber(request.getTelephoneNumber().orElse(null))
        .deviceKey(toDomain(request.getDeviceKey()))
        .build();
  }

  public Jwk toDomain(EcJwkRequest request) {
    return new Jwk(
        request.getKty(),
        request.getKid(),
        request.getAlg().orElse(null),
        request.getUse().orElse(null),
        request.getCrv(),
        request.getX(),
        request.getY());
  }
}
