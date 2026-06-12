// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.hsm;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.UUID;

@RecordBuilder
public record HsmOperationResult(
    UUID id,
    HsmAsyncStatus status,
    String result,
    String resultUrl,
    String stateJws) {
}
