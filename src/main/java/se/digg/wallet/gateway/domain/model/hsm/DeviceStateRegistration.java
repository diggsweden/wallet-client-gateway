// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.hsm;

import java.util.Optional;

public record DeviceStateRegistration(
    EcPublicJwk walletKey,
    boolean overwrite,
    Optional<String> ttl) {
}
