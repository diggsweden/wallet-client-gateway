// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/private/user")
public class SessionTestController {

  public record SessionTest(String accountId) {

  }

  @GetMapping("/session/test")
  public ResponseEntity<SessionTest> testSession() {
    return ResponseEntity
        .ok(new SessionTest(SecurityContextHolder.getContext().getAuthentication().getName()));
  }

  @GetMapping("/session/logout")
  public ResponseEntity<SessionTest> logoutSession(HttpSession session) {
    session.invalidate();
    return ResponseEntity.ok().build();
  }
}
