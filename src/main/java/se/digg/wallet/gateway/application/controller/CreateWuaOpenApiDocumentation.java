// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
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
