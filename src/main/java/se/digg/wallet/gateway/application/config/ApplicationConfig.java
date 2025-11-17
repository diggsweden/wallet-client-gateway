// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "properties")
@Validated
public record ApplicationConfig(
    @NotBlank String apisecret,
    @NotEmpty List<String> publicPaths,
    @NotNull Walletprovider walletprovider,
    @NotNull Attributeattestation attributeattestation,
    @NotNull Walletaccount walletaccount,
    @NotNull ChallengeCache challengeCache) {

  public record Walletprovider(@NotBlank String baseurl, @NotBlank String wuaPath) {
  }

  public record Attributeattestation(@NotBlank String baseurl, @NotNull Paths paths) {
    public record Paths(@NotBlank String post, @NotBlank String getById,
        @NotBlank String getByKey) {
    }
  }

  public record Walletaccount(@NotBlank String baseurl, @NotBlank Paths paths) {
    public record Paths(@NotBlank String post, @NotBlank String get) {
    }
  }

  public record ChallengeCache(@NotBlank int ttlSeconds) {
  }
}

