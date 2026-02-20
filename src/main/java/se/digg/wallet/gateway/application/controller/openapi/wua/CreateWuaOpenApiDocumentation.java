// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.openapi.wua;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.MediaType;
import se.digg.wallet.gateway.application.model.common.BadRequestDto;
import se.digg.wallet.gateway.application.model.wua.WuaDto;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(description = "Wua created", responseCode = "201",
    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = WuaDto.class)))
@ApiResponse(description = "Could not create Wua", responseCode = "500",
    content = @Content())
@ApiResponse(description = "Not authorized", responseCode = "401",
    content = @Content())
@ApiResponse(description = "Bad input", responseCode = "400",
    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
        schema = @Schema(implementation = BadRequestDto.class)))
public @interface CreateWuaOpenApiDocumentation {
}
