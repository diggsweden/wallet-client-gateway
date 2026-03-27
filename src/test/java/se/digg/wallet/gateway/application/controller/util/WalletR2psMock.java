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
    name = "wallet-r2ps-mock",
    portProperties = "wiremock.r2ps.port"))
public @interface WalletR2psMock {

  String NAME = "wallet-r2ps-mock";

}
