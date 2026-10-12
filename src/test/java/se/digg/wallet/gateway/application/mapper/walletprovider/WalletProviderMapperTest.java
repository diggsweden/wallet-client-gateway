// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.walletprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.digg.wallet.gateway.api.v0.model.EcJwkRequest;
import se.digg.wallet.gateway.application.model.EcJwkRequestTestBuilder;
import se.digg.wallet.gateway.domain.model.attestation.KeyAttestation;
import se.digg.wallet.gateway.domain.model.common.Jwk;

class WalletProviderMapperTest {

  private final WalletProviderMapper mapper = new WalletProviderMapper();

  @Test
  void an_ec_jwk_requests_fields_are_mapped_to_a_domain_jwk() {
    var request = EcJwkRequestTestBuilder.withDefaults().build();

    var result = mapper.toDomain(request);

    assertThat(result).isNotNull();
    assertThat(result.kty()).isEqualTo(request.getKty());
    assertThat(result.kid()).isEqualTo(request.getKid());
    assertThat(result.alg()).isEqualTo(request.getAlg().orElseThrow());
    assertThat(result.use()).isEqualTo(request.getUse().orElseThrow());
    assertThat(result.crv()).isEqualTo(request.getCrv());
    assertThat(result.x()).isEqualTo(request.getX());
    assertThat(result.y()).isEqualTo(request.getY());
  }

  @Test
  void absent_optional_ec_jwk_fields_are_mapped_to_null() {
    var request = EcJwkRequest.builder()
        .kty("EC")
        .kid("key-id")
        .crv("P-256")
        .x("x")
        .y("y")
        .build();

    var result = mapper.toDomain(request);

    assertThat(result.alg()).isNull();
    assertThat(result.use()).isNull();
  }

  @Test
  void a_list_of_ec_jwk_requests_is_mapped_in_order_and_null_entries_are_ignored() {
    var firstRequest = EcJwkRequestTestBuilder.withDefaults()
        .kid("first-key")
        .build();
    var secondRequest = EcJwkRequestTestBuilder.withDefaults()
        .kid("second-key")
        .build();
    var requests = new ArrayList<EcJwkRequest>();
    requests.add(firstRequest);
    requests.add(null);
    requests.add(secondRequest);

    var result = mapper.toDomain(requests);

    assertThat(result)
        .extracting(Jwk::kid)
        .containsExactly("first-key", "second-key");
  }

  @Test
  void an_empty_ec_jwk_request_list_is_mapped_to_an_empty_list() {
    assertThat(mapper.toDomain(List.of())).isEmpty();
  }

  @Test
  void a_key_attestations_jwt_is_mapped_to_the_response() {
    var keyAttestation = new KeyAttestation("signed.jwt.value");

    var result = mapper.toResponse(keyAttestation);

    assertThat(result).isNotNull();
    assertThat(result.getJwt()).isEqualTo(keyAttestation.jwt());
  }
}
