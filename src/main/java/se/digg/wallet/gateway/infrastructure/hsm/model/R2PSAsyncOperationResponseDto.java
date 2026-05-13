// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.hsm.model;

public record R2PSAsyncOperationResponseDto(
    String correlationId,
    String status,
    String result,
    String resultUrl,
    R2PSAsyncOperationErrorDto error) {
}
