// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.account;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.UUID;

@RecordBuilder
public record Account(
    UUID id,
    String personalIdentityNumber,
    String emailAdress,
    String telephoneNumber,
    Jwk deviceKey) {
}
