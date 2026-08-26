// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import se.digg.wallet.gateway.client.account.v0.model.AccountResponse;
import se.digg.wallet.gateway.client.account.v0.model.EcJwkItemsResponse;
import se.digg.wallet.gateway.client.account.v0.model.EcJwkResponse;
import se.digg.wallet.gateway.client.account.v0.model.SecurityEnvelopeResponse;
import se.digg.wallet.gateway.client.account.v0.model.SecurityEnvelopesResponse;
import se.digg.wallet.gateway.domain.model.account.Jwk;
import se.digg.wallet.gateway.domain.model.account.JwkBuilder;
import se.digg.wallet.gateway.domain.model.account.NewAccountBuilder;
import se.digg.wallet.gateway.domain.model.account.SecurityEnvelope;
import se.digg.wallet.gateway.infrastructure.account.mapper.AccountClientMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountClientMapperTest {

  private static final String PERSONAL_IDENTITY_NUMBER = "1910101010";
  private static final String EMAIL = "test.testsson@test.test";
  private static final String PHONE_NUMBER = "0700000000";

  private AccountClientMapper mapper;

  @BeforeEach
  void startup() {
    this.mapper = new AccountClientMapper();
  }

  @Test
  void mapNewAccountValuesToClientRequest() {

    var jwk = defaultJwk();

    var newAccount = NewAccountBuilder.builder()
        .personalIdentityNumber(PERSONAL_IDENTITY_NUMBER)
        .email(EMAIL)
        .phoneNumber(PHONE_NUMBER)
        .deviceKey(jwk)
        .build();

    var result = assertDoesNotThrow(() -> mapper.toClientRequest(newAccount));

    assertThat(result).isNotNull();
    assertThat(result.getPersonalIdentityNumber()).isEqualTo(newAccount.personalIdentityNumber());
    assertThat(result.getEmail()).isEqualTo(newAccount.email());
    assertThat(result.getPhoneNumber()).isEqualTo(newAccount.phoneNumber());
    var resultKey = result.getDeviceKey();
    assertThat(resultKey).isNotNull();
    assertThat(resultKey.getKid()).isEqualTo(jwk.kid());
    assertThat(resultKey.getKty()).isEqualTo(jwk.kty());
    assertThat(resultKey.getAlg()).isEqualTo(jwk.alg());
    assertThat(resultKey.getUse()).isEqualTo(jwk.use());
    assertThat(resultKey.getCrv()).isEqualTo(jwk.crv());
    assertThat(resultKey.getX()).isEqualTo(jwk.x());
    assertThat(resultKey.getY()).isEqualTo(jwk.y());
  }

  @Test
  void mapJwkValuesToClientRequest() {

    var jwk = defaultJwk();

    var result = assertDoesNotThrow(() -> mapper.toClientRequest(jwk));

    assertThat(result).isNotNull();
    assertThat(result).isNotNull();
    assertThat(result.getKid()).isEqualTo(jwk.kid());
    assertThat(result.getKty()).isEqualTo(jwk.kty());
    assertThat(result.getAlg()).isEqualTo(jwk.alg());
    assertThat(result.getUse()).isEqualTo(jwk.use());
    assertThat(result.getCrv()).isEqualTo(jwk.crv());
    assertThat(result.getX()).isEqualTo(jwk.x());
    assertThat(result.getY()).isEqualTo(jwk.y());
  }

  @Test
  void mapSecurityEnvelopeValuesToClientRequest() {

    var content = "the-content";
    var securityEnvelope = new SecurityEnvelope(content);

    var result = assertDoesNotThrow(() -> mapper.toClientRequest(securityEnvelope));

    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEqualTo(content);
  }

  static List<List<SecurityEnvelopeResponse>> emptySecurityEnvelopeItems() {

    var items = new ArrayList<List<SecurityEnvelopeResponse>>();
    items.add(null);
    items.add(Collections.emptyList());

    return items;
  }

  @ParameterizedTest
  @MethodSource("emptySecurityEnvelopeItems")
  void mapNullAndEmptySecurityEnvelopesResponseShouldReturnEmptyItems(
      List<SecurityEnvelopeResponse> emptyItems) {

    var response = SecurityEnvelopesResponse.builder()
        .items(emptyItems)
        .build();

    var result = assertDoesNotThrow(() -> mapper.toDomain(response));

    assertThat(result).isNotNull();
    assertThat(result.items()).isEmpty();
  }

  @Test
  void mapSecurityEnvelopesResponseValuesToDomainObject() {

    var content = "the-content";
    var response = SecurityEnvelopesResponse.builder()
        .items(List.of(
            SecurityEnvelopeResponse.builder()
                .content(content)
                .build()))
        .build();

    var result = assertDoesNotThrow(() -> mapper.toDomain(response));

    assertThat(result).isNotNull();
    assertThat(result.items()).isNotEmpty();
    assertThat(result.items()).hasSize(response.getItems().size());
    assertThat(result.items().getFirst().content()).isEqualTo(content);
  }

  @Test
  void mapAccountResponseValuesToDomainObject() {

    var responseKey = defaultEcJwkResponse();
    var response = AccountResponse.builder()
        .personalIdentityNumber(PERSONAL_IDENTITY_NUMBER)
        .email(EMAIL)
        .phoneNumber(PHONE_NUMBER)
        .deviceKey(responseKey)
        .build();

    var result = assertDoesNotThrow(() -> mapper.toDomain(response));

    assertThat(result).isNotNull();
    assertThat(result.personalIdentityNumber()).isEqualTo(response.getPersonalIdentityNumber());
    assertThat(result.email()).isEqualTo(response.getEmail());
    assertThat(result.phoneNumber()).isEqualTo(response.getPhoneNumber());
    var resultKey = result.deviceKey();
    assertThat(resultKey).isNotNull();
    assertThat(resultKey.kid()).isEqualTo(responseKey.getKid());
    assertThat(resultKey.kty()).isEqualTo(responseKey.getKty());
    assertThat(resultKey.alg()).isEqualTo(responseKey.getAlg());
    assertThat(resultKey.use()).isEqualTo(responseKey.getUse());
    assertThat(resultKey.crv()).isEqualTo(responseKey.getCrv());
    assertThat(resultKey.x()).isEqualTo(responseKey.getX());
    assertThat(resultKey.y()).isEqualTo(responseKey.getY());
  }

  @Test
  void mapEcJwkResponseValuesToDomainObject() {

    var responseKey = defaultEcJwkResponse();

    var result = assertDoesNotThrow(() -> mapper.toDomain(responseKey));

    assertThat(result).isNotNull();
    assertThat(result.kid()).isEqualTo(responseKey.getKid());
    assertThat(result.kty()).isEqualTo(responseKey.getKty());
    assertThat(result.alg()).isEqualTo(responseKey.getAlg());
    assertThat(result.use()).isEqualTo(responseKey.getUse());
    assertThat(result.crv()).isEqualTo(responseKey.getCrv());
    assertThat(result.x()).isEqualTo(responseKey.getX());
    assertThat(result.y()).isEqualTo(responseKey.getY());
  }

  static List<List<EcJwkResponse>> emptyEcJwkResponse() {

    var items = new ArrayList<List<EcJwkResponse>>();
    items.add(null);
    items.add(Collections.emptyList());

    return items;
  }

  @ParameterizedTest
  @MethodSource("emptyEcJwkResponse")
  void mapNullAndEmptyEcJwkItemsThrowsIllegalStateException(List<EcJwkResponse> emptyItems) {

    var response = EcJwkItemsResponse.builder()
        .items(emptyItems)
        .build();

    assertThrows(IllegalStateException.class, () -> mapper.toDomainJwk(response));
  }

  @Test
  void mapEcJwkItemsResponseValuesToDomainObject() {

    var responseKey = defaultEcJwkResponse();
    var response = EcJwkItemsResponse.builder()
        .items(List.of(responseKey))
        .build();

    var result = assertDoesNotThrow(() -> mapper.toDomainJwk(response));

    assertThat(result).isNotNull();
    assertThat(result.kid()).isEqualTo(responseKey.getKid());
    assertThat(result.kty()).isEqualTo(responseKey.getKty());
    assertThat(result.alg()).isEqualTo(responseKey.getAlg());
    assertThat(result.use()).isEqualTo(responseKey.getUse());
    assertThat(result.crv()).isEqualTo(responseKey.getCrv());
    assertThat(result.x()).isEqualTo(responseKey.getX());
    assertThat(result.y()).isEqualTo(responseKey.getY());
  }

  private Jwk defaultJwk() {
    return JwkBuilder.builder()
        .kid("kid")
        .kty("kty")
        .alg("alg")
        .use("use")
        .crv("crv")
        .x("x")
        .y("y")
        .build();
  }

  private EcJwkResponse defaultEcJwkResponse() {
    return EcJwkResponse.builder()
        .kid("kid")
        .kty("kty")
        .alg("alg")
        .use("use")
        .crv("crv")
        .x("x")
        .y("y")
        .build();
  }
}
