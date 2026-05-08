// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model.hsm;

import java.util.Optional;

public record RegisterStateRequestDto(EcPublicJwkDto publicKey, boolean overwrite, Optional<String> ttl) {
}
