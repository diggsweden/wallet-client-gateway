// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2
package se.digg.wallet.gateway.application.mapper.account;

import java.util.List;

import org.springframework.stereotype.Component;

import se.digg.wallet.gateway.api.v0.model.CreateAccountRequest;
import se.digg.wallet.gateway.api.v0.model.CreateAccountResponse;
import se.digg.wallet.gateway.api.v0.model.EcJwkRequest;
import se.digg.wallet.gateway.api.v0.model.SecurityEnvelopeResponse;
import se.digg.wallet.gateway.api.v0.model.SecurityEnvelopesResponse;
import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelopes;

@Component
public class AccountMapper {

  public CreateAccountResponse toResponse(Account account) {
    return CreateAccountResponse
        .builder()
        .accountId(account.id())
        .build();
  }

  public SecurityEnvelopesResponse toResponse(SecurityEnvelopes envelopes) {
    List<SecurityEnvelopeResponse> items = envelopes.items().stream()
        .map(e -> SecurityEnvelopeResponse.builder().content(e.content()).build())
        .toList();
    return SecurityEnvelopesResponse.builder().items(items).build();
  }

  public NewAccount toDomain(CreateAccountRequest request) {
    return new NewAccount(
        request.getPersonalIdentityNumber().orElse(null),
        request.getEmail().orElse(request.getEmailAdress().orElse(null)),
        request.getTelephoneNumber().orElse(null),
        toDomain(request.getDeviceKey()));
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
