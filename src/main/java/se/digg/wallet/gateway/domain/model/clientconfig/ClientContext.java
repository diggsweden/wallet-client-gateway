// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.clientconfig;

import jakarta.annotation.Nullable;

/**
 * What is known about the calling client, used to target feature flag values.
 */
public record ClientContext(
    @Nullable String os,
    @Nullable String osVersion,
    @Nullable String deviceModel,
    @Nullable String appVersion) {

  public static ClientContext empty() {
    return new ClientContext(null, null, null, null);
  }
}
