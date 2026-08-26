// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.account;

import se.digg.wallet.gateway.domain.model.account.JwkBuilder;

public class JwkTestBuilder {

  public static JwkBuilder withDefaults() {
    return JwkBuilder.builder()
        .kid("kid")
        .kty("kty")
        .alg("alg")
        .use("use")
        .crv("crv")
        .x("x")
        .y("y");
  }
}
