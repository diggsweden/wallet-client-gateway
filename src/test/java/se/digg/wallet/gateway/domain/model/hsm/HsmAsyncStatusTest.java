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
  void a_status_string_matches_case_insensitively_to_the_corresponding_hsm_async_status(
      String value) {
    var matchedEnum = assertDoesNotThrow(() -> HsmAsyncStatus.fromValue(value));
    assertThat(matchedEnum).isNotNull();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {
      "123456",
      "ABCDEF"
  })
  void a_null_blank_or_unrecognized_value_throws_illegal_argument_exception(String value) {
    assertThrows(IllegalArgumentException.class, () -> HsmAsyncStatus.fromValue(value));
  }
}
