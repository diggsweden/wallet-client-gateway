// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.MediaType;
import se.digg.wallet.gateway.application.model.BadRequestDto;
import se.digg.wallet.gateway.application.model.WuaDto;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
    @ApiResponse(description = "Wua created", responseCode = "201",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = WuaDto.class))),
    @ApiResponse(description = "Could not create Wua", responseCode = "500",
        content = @Content()),
    @ApiResponse(description = "Not authorized", responseCode = "401",
        content = @Content()),
    @ApiResponse(description = "Bad input", responseCode = "400",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = BadRequestDto.class)))
})
@RequestBody(content = @Content(
    examples = @ExampleObject("""
            {
              "walletId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
              "jwk": {
                "kty": "EC",
                "crv": "P-256",
                "x": "1fH0eqXgMMwCIafNaDc1axdCjLlw7zpTLvLWjpPvhEc",
                "y": "5qOejJs7BK-jLingaUTEhBrzP_YPyHfptS5yWE98I40"
              }
            }
        """)))
public @interface CreateWuaOpenApiDocumentation {
}
