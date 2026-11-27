// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.util;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(SpanContextExtension.class)
public @interface WithSpanContext {

  String DEFAULT_TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
  String DEFAULT_SPAN_ID = "00f067aa0ba902b7";

  String traceId() default DEFAULT_TRACE_ID;

  String spanId() default DEFAULT_SPAN_ID;

  boolean sampled() default true;
}
