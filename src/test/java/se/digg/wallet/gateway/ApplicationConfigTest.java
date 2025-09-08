// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import se.digg.wallet.gateway.application.config.ApplicationConfig;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(
    exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        LiquibaseAutoConfiguration.class
    })
public class ApplicationConfigTest {

  public static final String SECRET_TEST_VALUE = "my-super-secret-test-value";
  public static final String TEST_SERVICE_URL = "http://test-service:8888";
  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();
  @Autowired
  private ApplicationConfig applicationConfig;

  /**
   * This static method is called before the application context is created. It adds properties that
   * will be used to resolve the placeholders in the YAML file. This acts like setting an
   * environment variable
   */
  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("API_SECRET", () -> SECRET_TEST_VALUE);
    registry.add("DOWNSTREAM_SERVICE_URL", () -> TEST_SERVICE_URL);
  }

  @Test
  void testApplicationConfig() {
    // Assert that the values from our dynamic properties were loaded into the application config
    assertEquals(SECRET_TEST_VALUE, applicationConfig.apiSecret());
    assertEquals(TEST_SERVICE_URL, applicationConfig.downstreamServiceUrl());
  }

  @Test
  void testApplicationConfigWithBlankApiSecret() {
    this.contextRunner
        .withPropertyValues("API_SECRET=", "DOWNSTREAM_SERVICE_URL=")
        .withUserConfiguration(ApplicationConfig.class)
        .run(context -> assertThat(context).hasFailed());
  }
}
