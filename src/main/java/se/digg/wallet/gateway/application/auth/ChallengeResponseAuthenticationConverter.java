// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;
import se.digg.wallet.gateway.application.controller.exception.BadRequestException;
import se.digg.wallet.gateway.application.model.auth.AuthChallengeResponseDto;

@Component
public class ChallengeResponseAuthenticationConverter implements AuthenticationConverter {


  @Override
  public Authentication convert(HttpServletRequest request) {
    try {
      if (request.getContentLength() > 10000) {
        throw new BadRequestException(
            "Invalid request, content length: %s".formatted(request.getContentLength()));
      }
      var bytes = request.getInputStream().readAllBytes();
      var dto = new ObjectMapper().readValue(bytes, AuthChallengeResponseDto.class);

      return new ChallengeResponseAuthenticationToken(dto, null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
