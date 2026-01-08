// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.util;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/oidc/")
public class OidcTestController {

  public record SessionTest(String accountId) {

  }

  @GetMapping("/session/test")
  public ResponseEntity<SessionTest> testSession(
      @AuthenticationPrincipal OidcUser oidcUser) {
    return ResponseEntity
        .ok(new SessionTest(oidcUser.getSubject()));
  }

  @GetMapping("/session/logout")
  public ResponseEntity<SessionTest> logoutSession(HttpSession session) {
    session.invalidate();
    return ResponseEntity.ok().build();
  }
}
