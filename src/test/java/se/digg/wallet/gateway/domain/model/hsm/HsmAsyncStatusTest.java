// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.model.hsm;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HsmAsyncStatusTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "COMPLETE",
      "complete",
      "Complete",
      "PENDING",
      "pending",
      "Pending",
      "ERROR",
      "error",
      "Error"
  })
  void matchingValueReturnsEnum(String value) {
    var matchedEnum = assertDoesNotThrow(() -> HsmAsyncStatus.fromValue(value));
    assertThat(matchedEnum).isNotNull();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {
      "123456",
      "ABCDEF"
  })
  void nonMatchingValueThrowsIllegalArgumentException(String value) {
    assertThrows(IllegalArgumentException.class, () -> HsmAsyncStatus.fromValue(value));
  }
}
