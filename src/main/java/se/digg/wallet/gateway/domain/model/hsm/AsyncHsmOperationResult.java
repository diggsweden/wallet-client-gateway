// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.hsm;

public record AsyncHsmOperationResult(
    String correlationId,
    String status,
    String result,
    String resultUrl,
    AsyncHsmOperationError error) {
}
