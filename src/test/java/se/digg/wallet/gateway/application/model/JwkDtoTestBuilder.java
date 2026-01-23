// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

import com.nimbusds.jose.jwk.ECKey;
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

  public static JwkDtoBuilder withGeneratedKeyValues(ECKey generatedKeyPair) {
    return JwkDtoBuilder.builder()
        .alg(generatedKeyPair.getAlgorithm().toString())
        .kty(generatedKeyPair.getKeyType().getValue())
        .kid(generatedKeyPair.getKeyID())
        .crv(generatedKeyPair.getCurve().toString())
        .x(generatedKeyPair.getX().toString())
        .y(generatedKeyPair.getY().toString())
        .use(generatedKeyPair.getKeyUse().toString());
  }


}
