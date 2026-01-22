// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.oidc;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.digg.wallet.gateway.application.auth.DeepLinkHtmlGenerator;

@RestController
@RequestMapping("/oidc/auth")
public class OidcAuthController {

  private final DeepLinkHtmlGenerator htmlGenerator;

  public OidcAuthController(DeepLinkHtmlGenerator htmlGenerator) {
    this.htmlGenerator = htmlGenerator;
  }

  @GetMapping
  public ResponseEntity<String> getAppDeeplink(HttpSession oidcSession) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .contentType(MediaType.TEXT_HTML)
        .body(htmlGenerator.generate(oidcSession.getId()));
  }
}
