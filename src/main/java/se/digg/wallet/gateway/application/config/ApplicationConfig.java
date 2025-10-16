// SPDX-FileCopyrightText: 2025 diggsweden/wallet-backend-reference
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "properties")
@Validated
public record ApplicationConfig(
    @NotBlank String apisecret, Walletprovider walletprovider,
    Attributeattestation attributeattestation) {
  public record Walletprovider(@NotBlank String baseurl, @NotBlank String wuaPath) {
  }

  public record Attributeattestation(@NotBlank String baseurl, @NotNull AttributePaths paths) {
  }
}

