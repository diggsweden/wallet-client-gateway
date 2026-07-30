// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

import com.nimbusds.jose.jwk.ECKey;

import se.digg.wallet.gateway.api.v0.model.EcJwkRequest;

public class EcJwkRequestTestBuilder {

  public static EcJwkRequest.Builder withDefaults() {
    return EcJwkRequest.builder()
        .alg("ALG")
        .kty("KTY")
        .kid("KID")
        .crv("CRV")
        .x("X")
        .y("Y")
        .use("USE");
  }

  public static EcJwkRequest.Builder of(ECKey key) {
    return EcJwkRequest.builder()
        .alg(key.getAlgorithm().toString())
        .kty(key.getKeyType().getValue())
        .kid(key.getKeyID())
        .crv(key.getCurve().toString())
        .x(key.getX().toString())
        .y(key.getY().toString())
        .use(key.getKeyUse().toString());
  }
}
