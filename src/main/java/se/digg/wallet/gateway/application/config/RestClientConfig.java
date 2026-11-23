// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.config;

import io.micrometer.observation.ObservationRegistry;
import java.net.http.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import se.digg.wallet.gateway.client.account.v0.api.AccountApi;
import se.digg.wallet.gateway.client.hsm.v1.api.HandlersApi;
import se.digg.wallet.gateway.client.provider.v0.api.KeyAttestationApi;
import se.digg.wallet.gateway.client.provider.v0.api.WalletInstanceAttestationApi;
import se.digg.wallet.gateway.client.provider.v0.api.WalletUnitAttestationApi;

/**
 * Clients for the services behind the gateway. Each keeps the generated client's message converters
 * and adds the timeouts from {@link DownstreamHttpConfig} and an observation per call, which
 * records {@code http.client.requests}.
 */
@Configuration
public class RestClientConfig {

  private final ClientHttpRequestFactory requestFactory;
  private final ObservationRegistry observationRegistry;

  public RestClientConfig(DownstreamHttpConfig config, ObservationRegistry observationRegistry) {
    // HTTP/1.1: the JDK client defaults to HTTP/2 and would attempt an h2c upgrade on plain http
    var httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(config.connectTimeout())
        .build();
    var factory = new JdkClientHttpRequestFactory(httpClient);
    factory.setReadTimeout(config.readTimeout());
    this.requestFactory = factory;
    this.observationRegistry = observationRegistry;
  }

  private RestClient build(RestClient.Builder generatedBuilder) {
    return generatedBuilder
        .requestFactory(requestFactory)
        .observationRegistry(observationRegistry)
        .build();
  }

  /**
   * Creates a client bean to be used for remote calls to the Wallet Account API.
   *
   * @return an AccountAPI instance.
   */
  @Bean
  public AccountApi accountApi(@Value("${properties.walletaccount.baseurl}") String basePath) {
    var apiClient = new se.digg.wallet.gateway.client.account.v0.ApiClient(build(
        se.digg.wallet.gateway.client.account.v0.ApiClient.buildRestClientBuilder()));
    apiClient.setBasePath(basePath);
    return new AccountApi(apiClient);
  }

  /**
   * Creates a client bean to be used for remote calls to the Wallet HSM API. (aka
   * wallet-r2ps/wallet-bff).
   *
   * @return an HsmAPI instance.
   */
  @Bean
  public HandlersApi hsmApi(@Value("${properties.wallet-r2ps.baseurl}") String basePath) {
    var apiClient = new se.digg.wallet.gateway.client.hsm.v1.ApiClient(build(
        se.digg.wallet.gateway.client.hsm.v1.ApiClient.buildRestClientBuilder()));
    apiClient.setBasePath(basePath);
    return new HandlersApi(apiClient);
  }

  /**
   * Creates a client bean to be used for remote calls to the Wallet Provider API.
   *
   * @return a WalletUnitAttestationAPI instance.
   */
  @Bean
  public WalletUnitAttestationApi walletUnitAttestationApi(
      @Value("${properties.walletprovider.baseurl}") String basePath) {
    return new WalletUnitAttestationApi(providerApiClient(basePath));
  }

  /**
   * Creates a Key Attestation API client.
   *
   * @return a KeyAttestationAPI instance.
   */
  @Bean
  public KeyAttestationApi keyAttestationApi(
      @Value("${properties.walletprovider.baseurl}") String basePath) {
    return new KeyAttestationApi(providerApiClient(basePath));
  }

  /**
   * Creates a Wallet Instance Attestation API client.
   *
   * @return a WalletInstanceAttestationAPI instance.
   */
  @Bean
  public WalletInstanceAttestationApi walletInstanceAttestationApi(
      @Value("${properties.walletprovider.baseurl}") String basePath) {
    return new WalletInstanceAttestationApi(providerApiClient(basePath));
  }

  private se.digg.wallet.gateway.client.provider.v0.ApiClient providerApiClient(String basePath) {
    var apiClient = new se.digg.wallet.gateway.client.provider.v0.ApiClient(build(
        se.digg.wallet.gateway.client.provider.v0.ApiClient.buildRestClientBuilder()));
    apiClient.setBasePath(basePath);
    return apiClient;
  }
}
