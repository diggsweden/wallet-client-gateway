// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openApi(ServletContext servletContext) {
    return new OpenAPI()
        .info(new Info().title("Wallet Client Gateway").version("v1"))
        .servers(List.of(new Server().url(servletContext.getContextPath())))
        .components(new Components()
            .addSecuritySchemes("ApiKeyScheme",
                new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name(SecurityConfig.API_KEY_HEADER))
            .addSecuritySchemes("SessionIdScheme",
                new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name(SessionConfig.SESSION_HEADER)))
        .addSecurityItem(
            new SecurityRequirement().addList("ApiKeyScheme").addList("SessionIdScheme"));
  }

}
