// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@EnableWireMock(@ConfigureWireMock(
    name = "wallet-provider-mock",
    portProperties = "wiremock.provider.port"))
public @interface WalletProviderMock {

  String NAME = "wallet-provider-mock";

}
