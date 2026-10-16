// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.hsm;

import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.annotation.Nullable;

@RecordBuilder
public record DeviceStateRegistration(
    EcPublicJwk walletKey,
    @Nullable String ttl) {
}
