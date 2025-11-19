// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service.account;

import org.springframework.stereotype.Service;
import se.digg.wallet.gateway.application.model.account.CreateAccountRequestDto;
import se.digg.wallet.gateway.application.model.account.CreateAccountResponseDto;
import se.digg.wallet.gateway.infrastructure.account.client.WalletAccountClient;

@Service
public class AccountService {

  private final WalletAccountClient walletAccountClient;
  private final AccountMapper accountMapper;

  public AccountService(WalletAccountClient walletAccountClient, AccountMapper accountMapper) {
    this.walletAccountClient = walletAccountClient;
    this.accountMapper = accountMapper;
  }

  public CreateAccountResponseDto createAccount(CreateAccountRequestDto request,
      String personalIdentityNumber) {
    var mapped = accountMapper.toAccountCreateAccountDto(request, personalIdentityNumber);
    var result = walletAccountClient.createAccount(mapped);
    return new CreateAccountResponseDto(result.id());
  }
}
