// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2
package se.digg.wallet.gateway.application.mapper.account;

import java.util.List;

import org.springframework.stereotype.Component;

import se.digg.wallet.gateway.api.v0.model.CreateAccountRequest;
import se.digg.wallet.gateway.api.v0.model.CreateAccountRequestDto;
import se.digg.wallet.gateway.api.v0.model.CreateAccountResponseDto;
import se.digg.wallet.gateway.api.v0.model.KeyRequest;
import se.digg.wallet.gateway.api.v0.model.SecurityEnvelopeRequest;
import se.digg.wallet.gateway.api.v0.model.SecurityEnvelopeResponse;
import se.digg.wallet.gateway.api.v0.model.SecurityEnvelopesResponseDto;
import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelope;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelopes;

@Component
public class AccountMapper {

  public CreateAccountResponseDto toResponse(Account account) {
    return CreateAccountResponseDto
        .builder()
        .accountId(account.id())
        .build();
  }

  public NewAccount toDomain(CreateAccountRequestDto request) {
    return new NewAccount(
        request.getPersonalIdentityNumber().orElse(null),
        request.getEmailAdress().orElse(null),
        request.getTelephoneNumber().orElse(null),
        toDomain(request.getPublicKey()));
  }

  public NewAccount toDomain(CreateAccountRequest request) {
    return new NewAccount(
        request.getPersonalIdentityNumber().orElse(null),
        request.getEmailAdress().orElse(null),
        request.getTelephoneNumber().orElse(null),
        toDomain(request.getDeviceKey()));
  }

  public Jwk toDomain(KeyRequest request) {
    return new Jwk(
        request.getKty(),
        request.getKid(),
        request.getAlg().orElse(null),
        request.getUse().orElse(null),
        request.getCrv(),
        request.getX(),
        request.getY());
  }

  public SecurityEnvelope toDomain(SecurityEnvelopeRequest request) {
    return new SecurityEnvelope(request.getContent());
  }

  public SecurityEnvelopesResponseDto toResponse(SecurityEnvelopes envelopes) {
    List<SecurityEnvelopeResponse> items = envelopes.items().stream()
        .map(e -> SecurityEnvelopeResponse.builder().content(e.content()).build())
        .toList();
    return SecurityEnvelopesResponseDto.builder().items(items).build();
  }

}
