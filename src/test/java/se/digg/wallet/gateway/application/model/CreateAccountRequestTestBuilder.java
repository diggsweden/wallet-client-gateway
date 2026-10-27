// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

import se.digg.wallet.gateway.api.v0.model.CreateAccountRequest;

public class CreateAccountRequestTestBuilder {

  public static CreateAccountRequest.Builder withDefaults() {
    return CreateAccountRequest.builder()
        .deviceKey(EcJwkRequestTestBuilder.withDefaults().build());
  }
}
