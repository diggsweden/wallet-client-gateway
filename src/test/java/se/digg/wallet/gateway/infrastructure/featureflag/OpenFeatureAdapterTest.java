// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.featureflag;

import static org.assertj.core.api.Assertions.assertThat;

import dev.openfeature.contrib.providers.flagd.Config;
import dev.openfeature.contrib.providers.flagd.FlagdOptions;
import dev.openfeature.contrib.providers.flagd.FlagdProvider;
import dev.openfeature.sdk.OpenFeatureAPI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientContext;

class OpenFeatureAdapterTest {

  private static final String DOMAIN = OpenFeatureAdapterTest.class.getName();

  private static FlagdProvider provider;
  private static OpenFeatureAdapter adapter;

  @BeforeAll
  static void setUp() {
    provider = new FlagdProvider(FlagdOptions.builder()
        .resolverType(Config.Resolver.FILE)
        .offlineFlagSourcePath("src/test/resources/feature-flags.json")
        .build());
    OpenFeatureAPI.getInstance().setProviderAndWait(DOMAIN, provider);
    adapter = new OpenFeatureAdapter(OpenFeatureAPI.getInstance().getClient(DOMAIN));
  }

  @AfterAll
  static void tearDown() {
    provider.shutdown();
  }

  @Test
  void targetsOnClientContext() {
    var matching = new ClientContext("Android", "15", "Pixel 9", "2.1.0");
    var tooOld = new ClientContext("Android", "15", "Pixel 9", "1.9.0");
    var otherOs = new ClientContext("iOS", "18", "iPhone", "2.1.0");

    assertThat(adapter.getBoolean("client.feature.example", false, matching)).isTrue();
    assertThat(adapter.getBoolean("client.feature.example", false, tooOld)).isFalse();
    assertThat(adapter.getBoolean("client.feature.example", false, otherOs)).isFalse();
    assertThat(adapter.getBoolean("client.feature.example", false, ClientContext.empty()))
        .isFalse();
  }

  @Test
  void readsIntegerFlag() {
    assertThat(adapter.getInteger("client.cache-generation", 0, ClientContext.empty()))
        .isEqualTo(3);
  }

  @Test
  void returnsDefaultForMissingFlag() {
    assertThat(adapter.getBoolean("client.feature.missing", true, ClientContext.empty()))
        .isTrue();
  }

  @Test
  void returnsDefaultOnTypeMismatch() {
    assertThat(adapter.getInteger("client.feature.example", 7, ClientContext.empty()))
        .isEqualTo(7);
  }
}
