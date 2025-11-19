// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

public enum OidcClaims {
  PERSONAL_IDENTITY_NUMBER_CLAIM("https://id.oidc.se/claim/personalIdentityNumber");

  private String key;

  OidcClaims(String key) {
    this.key = key;
  }

  public String key() {
    return key;
  }
}
