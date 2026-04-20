// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

import se.digg.wallet.gateway.api.v0.model.CreateAccountRequest;

public class CreateAccountRequestTestBuilder {

  public static CreateAccountRequest.Builder withDefaults() {
    return CreateAccountRequest.builder()
        .personalIdentityNumber(CreateAccountRequestDtoTestBuilder.PERSONAL_IDENTITY_NUMBER)
        .emailAdress(CreateAccountRequestDtoTestBuilder.EMAIL_ADDRESS)
        .telephoneNumber(CreateAccountRequestDtoTestBuilder.TELEPHONE_NUMBER)
        .deviceKey(KeyRequestTestBuilder.withDefaults().build());
  }
}
