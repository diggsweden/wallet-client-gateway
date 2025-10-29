// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model.account;

public record AccountPublicKeyDto(
    String publicKeyBase64,
    String publicKeyIdentifier) {
}
