// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.account;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.util.Base64URL;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.api.v0.model.CreateAccountRequest;
import se.digg.wallet.gateway.api.v0.model.CreateAccountResponse;
import se.digg.wallet.gateway.api.v0.model.EcJwkRequest;
import se.digg.wallet.gateway.domain.model.account.Account;
import se.digg.wallet.gateway.domain.model.account.NewAccount;
import se.digg.wallet.gateway.domain.model.account.NewAccountBuilder;
import se.digg.wallet.gateway.domain.model.common.Jwk;

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
        .deviceKey(toDomain(request.getDeviceKey()))
        .build();
  }

  /** The RFC 7638 thumbprint of an EC public key, or empty if it is not a valid EC key. */
  public Optional<String> computeThumbprint(EcJwkRequest request) {
    try {
      return Optional.of(new ECKey.Builder(Curve.parse(request.getCrv()),
          new Base64URL(request.getX()), new Base64URL(request.getY()))
          .build()
          .computeThumbprint()
          .toString());
    } catch (IllegalArgumentException | IllegalStateException | JOSEException e) {
      return Optional.empty();
    }
  }

  public Jwk toDomain(EcJwkRequest request) {
    return new Jwk(
        request.getKty(),
        request.getKid(),
        request.getAlg(),
        request.getUse(),
        request.getCrv(),
        request.getX(),
        request.getY());
  }
}
