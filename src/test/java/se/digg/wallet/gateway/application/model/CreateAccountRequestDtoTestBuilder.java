// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

import java.util.Optional;
import se.digg.wallet.gateway.application.model.account.CreateAccountRequestDtoBuilder;

public class CreateAccountRequestDtoTestBuilder {

  public static final String PERSONAL_IDENTITY_NUMBER = "007";
  public static final String EMAIL_ADDRESS = "dig@digg.se";
  public static final String TELEPHONE_NUMBER = "070 007";

  public static CreateAccountRequestDtoBuilder withDefaults() {
    return CreateAccountRequestDtoBuilder.builder()
        .personalIdentityNumber(PERSONAL_IDENTITY_NUMBER)
        .emailAdress(EMAIL_ADDRESS)
        .telephoneNumber(Optional.of(TELEPHONE_NUMBER))
        .publicKey(JwkDtoTestBuilder.withDefaults().build());
  }


}
