// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

import se.digg.wallet.gateway.application.model.common.JwkDtoBuilder;

public class JwkDtoTestBuilder {

  public static JwkDtoBuilder withDefaults() {
    return JwkDtoBuilder.builder()
        .alg("ALG")
        .kty("KTY")
        .kid("KID")
        .crv("CRV")
        .x("X")
        .y("Y")
        .use("USE");
  }


}
