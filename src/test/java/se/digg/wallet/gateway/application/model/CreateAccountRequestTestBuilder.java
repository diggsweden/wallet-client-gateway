// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

import se.digg.wallet.gateway.api.v0.model.CreateAccountRequest;

public class CreateAccountRequestTestBuilder {

  public static final String PERSONAL_IDENTITY_NUMBER = "191010101010";
  public static final String EMAIL_ADDRESS = "test.testsson@test.test";
  public static final String TELEPHONE_NUMBER = "0700000000";

  public static CreateAccountRequest.Builder withDefaults() {
    return CreateAccountRequest.builder()
        .personalIdentityNumber(PERSONAL_IDENTITY_NUMBER)
        .email(EMAIL_ADDRESS)
        .telephoneNumber(TELEPHONE_NUMBER)
        .deviceKey(EcJwkRequestTestBuilder.withDefaults().build());
  }
}
