// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model.account;

import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import se.digg.wallet.gateway.application.model.common.JwkDto;

@RecordBuilder
public record CreateAccountRequestDto(
    Optional<String> personalIdentityNumber,
    @NotEmpty String emailAdress,
    Optional<String> telephoneNumber,
    @NotNull JwkDto publicKey) {
}
