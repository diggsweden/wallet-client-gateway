// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.account.model;

public record WalletAccountPublicKeyDto(
    String publicKeyBase64,
    String publicKeyIdentifier) {
}
