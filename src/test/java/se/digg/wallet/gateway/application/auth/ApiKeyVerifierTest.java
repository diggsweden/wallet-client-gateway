// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.SecurityConfig;
import se.digg.wallet.gateway.application.controller.exception.ApiKeyNeededException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyVerifierTest {

  private static final String VALID_KEY = "test-api-key";

  @Mock
  private ApplicationConfig config;

  private ApiKeyVerifier apiKeyVerifier;

  @BeforeEach
  void setUp() {
    when(config.apisecret()).thenReturn(VALID_KEY);
    apiKeyVerifier = new ApiKeyVerifier(config);
  }

  @AfterEach
  void tearDown() {
    RequestContextHolder.resetRequestAttributes();
  }

  private MockHttpServletRequest initRequestContextMock() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    return request;
  }

  @Test
  void throwsExceptionWhenKeysDoNotMatch() {
    initRequestContextMock().addHeader(SecurityConfig.API_KEY_HEADER, "wrong-key");

    assertThrows(ApiKeyNeededException.class, () -> apiKeyVerifier.verify());
  }

  @Test
  void acceptsMatchingApiKeys() {
    initRequestContextMock().addHeader(SecurityConfig.API_KEY_HEADER, VALID_KEY);

    assertDoesNotThrow(() -> apiKeyVerifier.verify());
  }

  @Test
  void throwsExceptionWhenNoKey() {
    initRequestContextMock();

    assertThrows(ApiKeyNeededException.class, () -> apiKeyVerifier.verify());
  }

}
