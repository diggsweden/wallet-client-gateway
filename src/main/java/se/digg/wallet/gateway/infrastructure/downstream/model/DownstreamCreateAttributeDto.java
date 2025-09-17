// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.downstream.model;

import jakarta.validation.constraints.NotBlank;

public record DownstreamCreateAttributeDto(@NotBlank String value) {
}
