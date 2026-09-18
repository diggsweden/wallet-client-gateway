// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.account.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.client.account.v0.model.AccountRequestDto;
import se.digg.wallet.gateway.client.account.v0.model.AccountResponseDto;
import se.digg.wallet.gateway.client.account.v0.model.EcJwkRequestDto;
import se.digg.wallet.gateway.client.account.v0.model.EcJwkResponseDto;
import se.digg.wallet.gateway.client.account.v0.model.EcJwkItemsResponseDto;
import se.digg.wallet.gateway.client.account.v0.model.SecurityEnvelopeRequestDto;
import se.digg.wallet.gateway.client.account.v0.model.SecurityEnvelopesResponseDto;
import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.AccountBuilder;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.JwkBuilder;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelope;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelopes;

@Component
public class AccountClientMapper {

  public AccountRequestDto toClientRequest(NewAccount newAccount) {
    return AccountRequestDto.builder()
        .email(newAccount.email())
        .phoneNumber(newAccount.phoneNumber())
        .personalIdentityNumber(newAccount.personalIdentityNumber())
        .deviceKey(toClientRequest(newAccount.deviceKey()))
        .build();
  }

  public EcJwkRequestDto toClientRequest(Jwk deviceKey) {
    return EcJwkRequestDto.builder()
        .alg(deviceKey.alg())
        .crv(deviceKey.crv())
        .kid(deviceKey.kid())
        .kty(deviceKey.kty())
        .x(deviceKey.x())
        .y(deviceKey.y())
        .use(deviceKey.use())
        .build();
  }

  public SecurityEnvelopeRequestDto toClientRequest(SecurityEnvelope securityEnvelope) {
    return SecurityEnvelopeRequestDto.builder().content(securityEnvelope.content()).build();
  }

  public SecurityEnvelopes toDomain(SecurityEnvelopesResponseDto response) {
    List<SecurityEnvelope> items = response.getItems() == null
        ? List.of()
        : response.getItems().stream()
            .map(e -> new SecurityEnvelope(e.getContent()))
            .toList();
    return new SecurityEnvelopes(items);
  }

  public Account toDomain(AccountResponseDto response) {
    return AccountBuilder.builder()
        .personalIdentityNumber(response.getPersonalIdentityNumber())
        .email(response.getEmail())
        .phoneNumber(response.getPhoneNumber())
        .id(response.getId())
        .deviceKey(toDomain(response.getDeviceKey()))
        .build();
  }

  public Jwk toDomain(EcJwkResponseDto response) {
    return JwkBuilder.builder()
        .kid(response.getKid())
        .kty(response.getKty())
        .alg(response.getAlg())
        .use(response.getUse())
        .crv(response.getCrv())
        .x(response.getX())
        .y(response.getY())
        .build();
  }

  public Jwk toDomainJwk(EcJwkItemsResponseDto response) {
    if (response.getItems() == null || response.getItems().isEmpty()) {
      throw new IllegalStateException("No wallet key found for account");
    }
    return toDomain(response.getItems().getFirst());
  }
}
