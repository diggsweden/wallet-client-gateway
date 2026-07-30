// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.account;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record NewAccount(
    String personalIdentityNumber,
    String email,
    String phoneNumber,
    Jwk deviceKey) {
}
