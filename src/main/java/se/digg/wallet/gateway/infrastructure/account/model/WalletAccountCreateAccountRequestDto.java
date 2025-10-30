// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.account.model;

import java.util.Optional;

public record WalletAccountCreateAccountRequestDto(
    String personalIdentityNumber,
    String emailAdress,
    Optional<String> telephoneNumber,
    WalletAccountJwkDto publicKey) {
}
