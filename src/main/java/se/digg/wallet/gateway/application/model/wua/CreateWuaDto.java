// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model.wua;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.digg.wallet.gateway.application.model.common.JwkDto;


@Deprecated(since = "0.3.1", forRemoval = true)
public record CreateWuaDto(
    @NotNull(message = "Wallet id cannot be empty") UUID walletId,
    @Valid @NotNull(message = "A valid JWK is required") JwkDto jwk) {
  private static final Logger log = LoggerFactory.getLogger(CreateWuaDto.class);
  private static final AtomicBoolean warningEmitted = new AtomicBoolean(false);

  public CreateWuaDto {
    if (warningEmitted.compareAndSet(false, true)) {
      log.warn("Deprecated record {} was instantiated. "
          + "This record will not be supported in the future.", getClass().getSimpleName());
    }
  }
}
