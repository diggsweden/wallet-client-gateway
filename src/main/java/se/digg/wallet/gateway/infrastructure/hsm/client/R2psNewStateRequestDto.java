// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.client;

import java.util.Optional;

import se.digg.wallet.gateway.application.model.hsm.EcPublicJwkDto;

public record R2psNewStateRequestDto(EcPublicJwkDto publicKey, boolean overwrite, Optional<String> ttl) {
}
