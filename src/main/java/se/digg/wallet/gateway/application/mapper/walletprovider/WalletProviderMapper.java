// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.walletprovider;

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.api.v0.model.EcJwkRequest;
import se.digg.wallet.gateway.api.v0.model.KeyAttestationResponse;
import se.digg.wallet.gateway.domain.model.common.Jwk;
import se.digg.wallet.gateway.domain.model.common.JwkBuilder;
import se.digg.wallet.gateway.domain.model.attestation.KeyAttestation;

@Component
public class WalletProviderMapper {

  public List<Jwk> toDomain(List<EcJwkRequest> keysRequest) {

    return keysRequest.stream()
        .filter(Objects::nonNull)
        .map(keyRequest -> JwkBuilder.builder()
            .kty(keyRequest.getKty())
            .kid(keyRequest.getKid())
            .alg(keyRequest.getAlg().orElse(null))
            .use(keyRequest.getUse().orElse(null))
            .crv(keyRequest.getCrv())
            .x(keyRequest.getX())
            .y(keyRequest.getY())
            .build())
        .toList();
  }

  public KeyAttestationResponse toResponse(KeyAttestation keyAttestation) {

    return KeyAttestationResponse.builder()
        .jwt(keyAttestation.jwt())
        .build();
  }
}
