// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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
  }

  @Test
  void testCreateWuaWithNullChallengeResponseAuthenticationReturnsBadRequest() {
    var response = wuaController.createWua(null, Optional.of("test-nonce"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNull();
    verify(wuaService, never()).createWua(anyString(), anyString());
  }

  @Test
  void testCreateWuaWithNullAccountIdReturnsUnauthorized() {
    // Arrange - Create a mock ChallengeResponseAuthentication with null accountId
    // Since the constructor requires non-null accountId, we'll mock it
    ChallengeResponseAuthentication mockAuth = mock(ChallengeResponseAuthentication.class);
    when(mockAuth.getAccountId()).thenReturn(null);

    var response = wuaController.createWua(mockAuth, Optional.of("test-nonce"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNull();
    verify(wuaService, never()).createWua(anyString(), anyString());
  }

  @Test
  void testCreateWuaWithEmptyNonceReturnsBadRequest() {
    // Arrange
    ChallengeResponseAuthentication auth = new ChallengeResponseAuthentication("test-account-id");

    // Act
    var response = wuaController.createWua(auth, Optional.of(""));

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNull();
    verify(wuaService, never()).createWua(anyString(), anyString());
  }

  @Test
  void testCreateWuaSuccessfullyReturnsCreated() {
    // Arrange
    ChallengeResponseAuthentication auth = new ChallengeResponseAuthentication("test-account-id");
    WuaDto expectedWuaDto = new WuaDto("test-wua-content");
    when(wuaService.createWua("test-account-id", "test-nonce")).thenReturn(expectedWuaDto);

    // Act
    var response = wuaController.createWua(auth, Optional.of("test-nonce"));

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isEqualTo(expectedWuaDto);
    verify(wuaService).createWua("test-account-id", "test-nonce");
  }

  @Test
  void testCreateWuaWithoutNonceSuccessfullyReturnsCreated() {
    // Arrange
    ChallengeResponseAuthentication auth = new ChallengeResponseAuthentication("test-account-id");
    WuaDto expectedWuaDto = new WuaDto("test-wua-content");
    when(wuaService.createWua("test-account-id", "")).thenReturn(expectedWuaDto);

    // Act
    var response = wuaController.createWua(auth, Optional.empty());

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isEqualTo(expectedWuaDto);
    verify(wuaService).createWua("test-account-id", "");
  }
}
