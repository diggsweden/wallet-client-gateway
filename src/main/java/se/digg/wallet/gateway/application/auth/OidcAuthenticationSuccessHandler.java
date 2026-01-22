// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OidcAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final DeepLinkHtmlGenerator htmlGenerator;

  public OidcAuthenticationSuccessHandler(DeepLinkHtmlGenerator htmlGenerator) {
    this.htmlGenerator = htmlGenerator;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    var session = request.getSession(false);
    var sessionId = session != null ? session.getId() : "";

    response.setStatus(200);
    response.setCharacterEncoding("utf-8");
    response.setContentType(MediaType.TEXT_HTML_VALUE);
    response.getWriter().write(htmlGenerator.generate(sessionId));
  }
}
