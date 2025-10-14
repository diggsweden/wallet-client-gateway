// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.openapi.attestation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.MediaType;
import se.digg.wallet.gateway.application.model.BadRequestDto;
import se.digg.wallet.gateway.infrastructure.attestation.model.AttestationDto;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(description = "Attestation found", responseCode = "200",
    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = AttestationDto.class)))
@ApiResponse(description = "Attestion not found", responseCode = "404",
    content = @Content())
@ApiResponse(description = "Unknown error", responseCode = "500",
    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = BadRequestDto.class)))

public @interface GetOpenApiDocumentation {
}
