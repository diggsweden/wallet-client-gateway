// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

import com.nimbusds.jose.jwk.ECKey;
import java.util.UUID;
import se.digg.wallet.gateway.domain.model.account.AccountBuilder;
import se.digg.wallet.gateway.domain.model.account.JwkBuilder;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountAccountDtoBuilder;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountJwkDtoBuilder;

public class WalletAccountAccountDtoTestBuilder {
  public static WalletAccountAccountDtoBuilder generateWalletAccount(ECKey publicEcKey) {
    return WalletAccountAccountDtoBuilder.builder()
        .emailAdress("dummy@dummy.se")
        .id(UUID.randomUUID())
        .personalIdentityNumber("19770701-1234")
        .publicKey(WalletAccountJwkDtoBuilder.builder()
            .kty(publicEcKey.getKeyType().getValue())
            .crv(publicEcKey.getCurve().toString())
            .x(publicEcKey.getX().toString())
            .y(publicEcKey.getY().toString())
            .alg(publicEcKey.getAlgorithm().toString())
            .use(publicEcKey.getKeyUse().getValue())
            .kid(publicEcKey.getKeyID()).build());
  }

  public static AccountBuilder generateAccount(ECKey ecKey) {
    return AccountBuilder.builder()
        .id(UUID.randomUUID())
        .emailAdress("dummy@dummy.se")
        .personalIdentityNumber("197707011234")
        .deviceKey(JwkBuilder.builder()
            .kty(ecKey.getKeyType().getValue())
            .crv(ecKey.getCurve().toString())
            .x(ecKey.getX().toString())
            .y(ecKey.getY().toString())
            .alg(ecKey.getAlgorithm().toString())
            .use(ecKey.getKeyUse().getValue())
            .kid(ecKey.getKeyID())
            .build());
  }

}
