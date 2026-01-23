// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.oidc;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oidc/auth")
public class OidcAuthController {
  @GetMapping
  public ResponseEntity<String> getAppDeeplink(HttpSession oidcSession) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .build();
  }
}
