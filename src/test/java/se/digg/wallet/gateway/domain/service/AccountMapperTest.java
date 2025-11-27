// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder;
import se.digg.wallet.gateway.domain.service.account.AccountMapper;

class AccountMapperTest {

  @Test
  void testMapping() {
    var requestDto = CreateAccountRequestDtoTestBuilder.withDefaults().build();
    var mapped = new AccountMapper().toAccountCreateAccountDto(requestDto, null);
    assertThat(mapped)
        .hasFieldOrPropertyWithValue("personalIdentityNumber",
            requestDto.personalIdentityNumber().get())
        .usingRecursiveComparison()
        .ignoringFields("personalIdentityNumber")
        .isEqualTo(requestDto);
  }

  @Test
  void testMappingOverridingPersonalIdentityNumber() {
    var personalIdentityNumber = "123123123";
    var requestDto = CreateAccountRequestDtoTestBuilder.withDefaults().build();
    var mapped = new AccountMapper().toAccountCreateAccountDto(requestDto, personalIdentityNumber);
    assertThat(mapped)
        .hasFieldOrPropertyWithValue("personalIdentityNumber", personalIdentityNumber)
        .usingRecursiveComparison()
        .ignoringFields("personalIdentityNumber")
        .isEqualTo(requestDto);
  }

}
