// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.account;

import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.application.model.account.CreateAccountRequestDto;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountCreateAccountRequestDto;
import se.digg.wallet.gateway.infrastructure.account.model.WalletAccountPublicKeyDto;

@Component

public class AccountMapper {
  public WalletAccountCreateAccountRequestDto toAccountCreateAccountDto(
      CreateAccountRequestDto dto) {
    return new WalletAccountCreateAccountRequestDto(
        dto.personalIdentityNumber(),
        dto.emailAdress(),
        dto.telephoneNumber(),
        new WalletAccountPublicKeyDto(
            dto.publicKey().publicKeyBase64(),
            dto.publicKey().publicKeyIdentifier()));
  }
}
