// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.account;

import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.api.v0.model.CreateAccountRequestDto;
import se.digg.wallet.gateway.api.v0.model.CreateAccountResponseDto;
import se.digg.wallet.gateway.api.v0.model.EcJwkRequestDto;
import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.model.account.NewAccountBuilder;

@Component
public class AccountMapper {

  public CreateAccountResponseDto toResponse(Account account) {
    return CreateAccountResponseDto
        .builder()
        .accountId(account.id())
        .build();
  }

  public NewAccount toDomain(CreateAccountRequestDto request) {
    return NewAccountBuilder.builder()
        .personalIdentityNumber(request.getPersonalIdentityNumber().orElse(null))
        .email(request.getEmail().orElse(request.getEmailAdress().orElse(null)))
        .phoneNumber(request.getTelephoneNumber().orElse(null))
        .deviceKey(toDomain(request.getDeviceKey()))
        .build();
  }

  public Jwk toDomain(EcJwkRequestDto request) {
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
