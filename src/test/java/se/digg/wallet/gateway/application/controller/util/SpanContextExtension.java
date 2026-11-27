// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller.util;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

public class SpanContextExtension implements BeforeEachCallback, AfterEachCallback {

  private static final ExtensionContext.Namespace NAMESPACE =
      ExtensionContext.Namespace.create(SpanContextExtension.class);
  private static final String SCOPE_KEY = "otel.scope";

  @Override
  public void beforeEach(ExtensionContext context) {
    WithSpanContext annotation = AnnotationSupport
        .findAnnotation(context.getElement(), WithSpanContext.class)
        .or(() -> AnnotationSupport.findAnnotation(context.getTestClass(), WithSpanContext.class))
        .orElseThrow(() -> new IllegalStateException("@WithSpanContext annotation not found"));

    SpanContext spanContext = SpanContext.create(
        annotation.traceId(),
        annotation.spanId(),
        annotation.sampled() ? TraceFlags.getSampled() : TraceFlags.getDefault(),
        TraceState.getDefault());

    if (!spanContext.isValid()) {
      throw new IllegalArgumentException(
          "Invalid traceId ('" + annotation.traceId() + "') or spanId ('" + annotation.spanId()
              + "'). " +
              "Must be 32 and 16 lowercase non-zero hex characters respectively.");
    }

    Scope scope = Span.wrap(spanContext).makeCurrent();
    context.getStore(NAMESPACE).put(SCOPE_KEY, scope);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    Scope scope = context.getStore(NAMESPACE).remove(SCOPE_KEY, Scope.class);
    if (scope != null) {
      scope.close();
    }
  }
}
