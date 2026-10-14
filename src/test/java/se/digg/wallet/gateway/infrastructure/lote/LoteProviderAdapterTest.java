// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.lote;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.convention.TestBean;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.domain.model.lote.LoteProvider;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = LoteProviderAdapter.class)
public class LoteProviderAdapterTest {

  @Autowired
  private LoteProviderAdapter loteProviderAdapter;

  @Nested
  class WhenConfigListIsMissing {

    @TestBean
    private ApplicationConfig applicationConfig;

    static ApplicationConfig applicationConfig() {
      return new ApplicationConfig(
          null,
          null,
          List.of(),
          List.of(),
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null);
    }

    @Test
    void serves_an_empty_list() {

      var loteProviders = loteProviderAdapter.getLoteProviders();
      assertThat(loteProviders).isEmpty();
    }
  }

  @Nested
  class WhenConfigListIsEmpty {

    @TestBean
    private ApplicationConfig applicationConfig;

    static ApplicationConfig applicationConfig() {
      return new ApplicationConfig(
          null,
          null,
          List.of(),
          List.of(),
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          List.of());
    }

    @Test
    void serves_an_empty_list() {

      var loteProviders = loteProviderAdapter.getLoteProviders();
      assertThat(loteProviders).isEmpty();
    }
  }

  @Nested
  class WhenConfigListIsPresent {

    @TestBean
    private ApplicationConfig applicationConfig;

    static ApplicationConfig applicationConfig() {
      return new ApplicationConfig(
          null,
          null,
          List.of(),
          List.of(),
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          List.of(
              new ApplicationConfig.LoteProviderItem("Test 1",
                  URI.create("https://first-url.test")),
              new ApplicationConfig.LoteProviderItem("Test 2",
                  URI.create("https://second-url.test"))));
    }

    @Test
    void serves_the_configured_list_of_lote_providers() {

      var result = loteProviderAdapter.getLoteProviders();
      assertThat(result)
          .containsExactly(
              new LoteProvider("Test 1", URI.create("https://first-url.test")),
              new LoteProvider("Test 2", URI.create("https://second-url.test")));
    }
  }
}
