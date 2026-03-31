// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import se.digg.wallet.gateway.application.auth.ChallengeResponseAuthentication;
import se.digg.wallet.gateway.application.model.wua.WuaDto;
import se.digg.wallet.gateway.domain.service.wua.WuaService;

@ExtendWith(MockitoExtension.class)
class WuaControllerUnitTest {

  @Mock
  private WuaService wuaService;

  private WuaController wuaController;

  @BeforeEach
  void setUp() {
    wuaController = new WuaController(wuaService);
    SecurityContextHolder.setContext(
        SecurityContextHolder.createEmptyContext());
  }

  @Mock
  private SecurityContextHolder securityContextHolder;

  @Test
  void testCreateWuaWithNullChallengeResponseAuthenticationReturnsBadRequest() {
    var response = wuaController.createWua(Optional.of("test-nonce"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNull();
    verify(wuaService, never()).createWua(anyString(), anyString());
  }

  /*
   * @Test void testCreateWuaWithNullAccountIdReturnsUnauthorized() { // Arrange - Create a mock
   * ChallengeResponseAuthentication with null accountId // Since the constructor requires non-null
   * accountId, we'll mock it
   *
   * //ChallengeResponseAuthentication mockAuth = mock(ChallengeResponseAuthentication.class);
   * //when(mockAuth.getAccountId()).thenReturn(null);
   *
   * SecurityContextHolder.getContext().setAuthentication( new ChallengeResponseAuthentication(null)
   * ); var response = wuaController.createWua(Optional.of("test-nonce"));
   *
   * assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
   * assertThat(response.getBody()).isNull(); verify(wuaService, never()).createWua(anyString(),
   * anyString()); }
   */

  @Test
  void testCreateWuaWithEmptyNonceReturnsBadRequest() {
    // Arrange
    ChallengeResponseAuthentication auth = new ChallengeResponseAuthentication("test-account-id");

    // Act
    var response = wuaController.createWua(Optional.of(""));

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNull();
    verify(wuaService, never()).createWua(anyString(), anyString());
  }

  @Test
  void testCreateWuaSuccessfullyReturnsCreated() {
    // Arrange
    var accountId = "test-account-id";
    var nonce = "test-nonce";
    var expectedJwt = "the.expected.jwt";

    SecurityContextHolder.getContext().setAuthentication(
        new ChallengeResponseAuthentication(accountId));

    WuaDto expectedWuaDto = new WuaDto(expectedJwt);
    when(wuaService.createWua(accountId, nonce)).thenReturn(expectedWuaDto);

    // Act
    var response = wuaController.createWua(Optional.of(nonce));

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var actualJwt = response.getBody().getJwt();
    assertEquals(expectedJwt, actualJwt);
    verify(wuaService).createWua(accountId, nonce);
  }

  @Test
  void testCreateWuaWithoutNonceSuccessfullyReturnsCreated() {
    // Arrange
    var accountId = "test-account-id";
    var expectedJwt = "the.expected.jwt";

    SecurityContextHolder.getContext().setAuthentication(
        new ChallengeResponseAuthentication(accountId));

    WuaDto expectedWuaDto = new WuaDto(expectedJwt);
    when(wuaService.createWua(accountId, "")).thenReturn(expectedWuaDto);

    // Act
    var response = wuaController.createWua(Optional.empty());

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var actualJwt = response.getBody().getJwt();
    assertEquals(expectedJwt, actualJwt);
    verify(wuaService).createWua(accountId, "");
  }
}
