// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.openapi.attestation;

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
import se.digg.wallet.gateway.infrastructure.attestation.model.ClientAttestationDto;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(description = "Attestation created", responseCode = "201",
    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = ClientAttestationDto.class)))
@ApiResponse(description = "Could not create attestation, server error", responseCode = "500",
    content = @Content())
@ApiResponse(description = "Bad input, unable to create attestation", responseCode = "400",
    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = BadRequestDto.class)))
@RequestBody(content = @Content(
    examples = @ExampleObject("""
            {
            "hsmId": "cbe80ad0-6a7d-4a5a-9891-8b4e95fa4d49",
            "wuaId": "790acda4-3dec-4d93-8efe-71375109d30e",
            "attestationData": "string"
          }
        """)))
public @interface PostOpenApiDocumentation {
}
