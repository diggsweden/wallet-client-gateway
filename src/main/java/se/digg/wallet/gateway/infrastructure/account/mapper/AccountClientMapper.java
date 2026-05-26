// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.account.mapper;

import org.springframework.stereotype.Component;

import se.digg.wallet.gateway.client.account.origin.model.AccountDto;
import se.digg.wallet.gateway.client.account.origin.model.CreateAccountRequestDto;
import se.digg.wallet.gateway.client.account.origin.model.PublicKeyDto;
import java.util.List;

import se.digg.wallet.gateway.client.account.v0.model.SecurityEnvelopeRequest;
import se.digg.wallet.gateway.client.account.v0.model.SecurityEnvelopesResponse;
import se.digg.wallet.gateway.client.account.v0.model.AccountRequest;
import se.digg.wallet.gateway.client.account.v0.model.AccountResponse;
import se.digg.wallet.gateway.client.account.v0.model.KeyRequest;
import se.digg.wallet.gateway.client.account.v0.model.KeyResponse;
import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.AccountBuilder;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.JwkBuilder;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelope;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelopes;

@Component
public class AccountClientMapper {


  public AccountRequest toClientRequest(NewAccount newAccount) {
    return AccountRequest.builder()
        .email(newAccount.emailAdress())
        .phoneNumber(newAccount.telephoneNumber())
        .personalIdentityNumber(newAccount.personalIdentityNumber())
        .deviceKey(toClientRequest(newAccount.deviceKey()))
        .build();
  }

  public KeyRequest toClientRequest(Jwk deviceKey) {
    return KeyRequest.builder()
        .alg(deviceKey.alg())
        .crv(deviceKey.crv())
        .kid(deviceKey.kid())
        .kty(deviceKey.kty())
        .x(deviceKey.x())
        .y(deviceKey.y())
        .use(deviceKey.use())
        .build();
  }

  public SecurityEnvelopeRequest toClientRequest(SecurityEnvelope securityEnvelope) {
    return SecurityEnvelopeRequest.builder().content(securityEnvelope.content()).build();
  }

  public SecurityEnvelopes toDomain(SecurityEnvelopesResponse response) {
    List<SecurityEnvelope> items = response.getItems() == null
        ? List.of()
        : response.getItems().stream()
            .map(e -> new SecurityEnvelope(e.getContent()))
            .toList();
    return new SecurityEnvelopes(items);
  }

  public CreateAccountRequestDto toOriginClientRequest(NewAccount newAccount) {
    return CreateAccountRequestDto.builder()
        .personalIdentityNumber(newAccount.personalIdentityNumber())
        .emailAdress(newAccount.emailAdress())
        .telephoneNumber(newAccount.telephoneNumber())
        .publicKey(toOriginClientRequest(newAccount.deviceKey()))
        .build();
  }

  public PublicKeyDto toOriginClientRequest(Jwk deviceKey) {
    return PublicKeyDto.builder()
        .kty(deviceKey.kty())
        .kid(deviceKey.kid())
        .alg(deviceKey.alg())
        .use(deviceKey.use())
        .crv(deviceKey.crv())
        .x(deviceKey.x())
        .y(deviceKey.y())
        .build();
  }

  public Account toDomain(AccountResponse response) {
    return AccountBuilder.builder()
        .emailAdress(response.getEmail())
        .telephoneNumber(response.getPhoneNumber())
        .id(response.getId())
        .deviceKey(toDomain(response.getDeviceKey()))
        .build();
  }

  public Account toDomain(AccountDto response) {
    return AccountBuilder.builder()
        .id(response.getId())
        .personalIdentityNumber(response.getPersonalIdentityNumber())
        .emailAdress(response.getEmailAdress())
        .telephoneNumber(response.getTelephoneNumber())
        .deviceKey(toDomain(response.getPublicKey()))
        .build();
  }

  public Jwk toDomain(KeyResponse response) {
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

  public Jwk toDomain(PublicKeyDto publicKey) {
    if (publicKey == null) {
      return null;
    }
    return JwkBuilder.builder()
        .kid(publicKey.getKid())
        .kty(publicKey.getKty())
        .alg(publicKey.getAlg())
        .use(publicKey.getUse())
        .crv(publicKey.getCrv())
        .x(publicKey.getX())
        .y(publicKey.getY())
        .build();
  }

}
