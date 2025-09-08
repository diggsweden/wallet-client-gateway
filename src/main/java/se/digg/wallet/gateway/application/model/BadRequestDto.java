// SPDX-FileCopyrightText: 2025 diggsweden/wallet-backend-reference
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

/** Compliant with RFC 9457 / https://www.dataportal.se/rest-api-profil/felhantering. */
public record BadRequestDto(
    String type, String title, int status, String detail, String instance) {
}
