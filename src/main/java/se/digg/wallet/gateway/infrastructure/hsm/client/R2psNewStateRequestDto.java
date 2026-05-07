// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.client;

import java.util.Optional;

public record R2psNewStateRequestDto(
    EcPublicJwkDto publicKey, boolean overwrite, Optional<String> ttl) {

  public record EcPublicJwkDto(String kty, String crv, String x, String y, String kid) {
  }
}
