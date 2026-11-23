// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Timeouts for calls to the services behind the gateway. Without them a service that accepts the
 * connection but never answers holds a request thread forever.
 *
 * @param connectTimeout maximum time to establish the TCP connection
 * @param readTimeout maximum time to wait for the response once the request is sent
 */
@ConfigurationProperties(prefix = "properties.downstream-http")
@Validated
public record DownstreamHttpConfig(
    @NotNull @DefaultValue("2s") Duration connectTimeout,
    @NotNull @DefaultValue("5s") Duration readTimeout) {
}
