// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.ApplicationConfig.OidcClaims;
import se.digg.wallet.gateway.application.controller.exception.BadRequestException;
import se.digg.wallet.gateway.application.controller.oidc.AccountControllerV1;
import se.digg.wallet.gateway.application.model.CreateAccountRequestDtoTestBuilder;
import se.digg.wallet.gateway.domain.service.account.AccountService;

@ExtendWith(MockitoExtension.class)
public class AccountControllerV1Test {

  private AccountControllerV1 controller;

  private AccountService service;
  private ApplicationConfig config;

  @BeforeEach
  public void beforeEach() {
    service = mock(AccountService.class);
    config = mock(ApplicationConfig.class);
    when(config.oidcClaims())
        .thenReturn(new OidcClaims("pnr"));

    controller = new AccountControllerV1(service, config);
  }

  @Test
  void createAccount() {
    var personalIdentityNumber = "199010001001";
    var oidcUser = new DefaultOidcUser(List.of(), new OidcIdToken(
        "tokenValue",
        Instant.now().minusSeconds(5),
        Instant.now().plusSeconds(5),
        Map.of("sub", "DonnyDuck",
            "pnr", personalIdentityNumber)));
    var oidcSession = new MockHttpSession();
    var requestDto = CreateAccountRequestDtoTestBuilder.withDefaults().build();
    var response = controller.createAccount(requestDto, oidcUser, oidcSession);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
    verify(service).createAccount(requestDto, personalIdentityNumber);
    assertThat(oidcSession.isInvalid()).isEqualTo(true);
  }

  @Test
  void invalidPersonalIdentityNumber() {
    var oidcUser = new DefaultOidcUser(List.of(), new OidcIdToken(
        "tokenValue",
        Instant.now().minusSeconds(5),
        Instant.now().plusSeconds(5),
        Map.of("sub", "DonnyDuck",
            "pnr", "19910001001")));
    var oidcSession = new MockHttpSession();
    assertThrows(BadRequestException.class,
        () -> {
          controller.createAccount(
              CreateAccountRequestDtoTestBuilder.withDefaults().build(),
              oidcUser, oidcSession);
        });
    verifyNoInteractions(service);
    assertThat(oidcSession.isInvalid()).isEqualTo(false);
  }
}
