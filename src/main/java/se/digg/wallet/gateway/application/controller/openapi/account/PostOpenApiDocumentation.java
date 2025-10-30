// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.openapi.account;

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
import se.digg.wallet.gateway.application.model.BadRequestDto;
import se.digg.wallet.gateway.application.model.account.CreateAccountResponseDto;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(description = "Account created", responseCode = "201",
    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = CreateAccountResponseDto.class)))
@ApiResponse(description = "Could not create account, server error", responseCode = "500",
    content = @Content())
@ApiResponse(description = "Bad input, unable to create account", responseCode = "400",
    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = BadRequestDto.class)))
@RequestBody(content = @Content(
    examples = @ExampleObject("""
            {
              "personalIdentityNumber": "01234",
              "emailAdress": "digg@example.com",
              "telephoneNumber": "070 007",
              "publicKey": {
                "kty": "EC",
                "crv": "P-256",
                "x": "1fH0eqXgMMwCIafNaDc1axdCjLlw7zpTLvLWjpPvhEc",
                "y": "5qOejJs7BK-jLingaUTEhBrzP_YPyHfptS5yWE98I40",
                "kid": "myKey"
              }
            }
        """)))
public @interface PostOpenApiDocumentation {
}
