// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;


import se.digg.wallet.gateway.api.v0.model.CreateAccountRequest;

public class CreateAccountRequestDtoTestBuilder {

  public static final String PERSONAL_IDENTITY_NUMBER = "198001022386";
  public static final String EMAIL_ADDRESS = "dig@digg.se";
  public static final String TELEPHONE_NUMBER = "070 007";

  public static CreateAccountRequest.Builder withDefaults() {
    return CreateAccountRequest.builder()
        .personalIdentityNumber(PERSONAL_IDENTITY_NUMBER)
        .emailAdress(EMAIL_ADDRESS)
        .telephoneNumber(TELEPHONE_NUMBER)
        .deviceKey(KeyRequestTestBuilder.withDefaults().build());
  }


}
