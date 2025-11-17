// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.openapi.auth;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.MediaType;
import se.digg.wallet.gateway.application.model.common.BadRequestDto;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)

@ApiResponse(description = "Session created", responseCode = "200", content = @Content())
@ApiResponse(description = "Unknown error", responseCode = "500",
    content = @Content())
@ApiResponse(description = "Bad input, session not created", responseCode = "400",
    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = BadRequestDto.class)))
@RequestBody(description = "Challenge response",
    content = @Content(
        examples = @ExampleObject("""
                {
                  "signedJwt": "insert.signed.jwt"
                }
            """)))
public @interface ChallengeResponseOpenApiDocumentation {
}
