// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.model;

import java.util.Optional;

public record R2PSDeviceStateRequestDto(
    R2PSEcPublicJwkDto publicKey, boolean overwrite, Optional<String> ttl) {
}
