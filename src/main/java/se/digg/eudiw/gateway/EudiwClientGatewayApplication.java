// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.eudiw.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the EUDIW Client Gateway.
 *
 * <p>This is the European Digital Identity Wallet Client Gateway application, providing secure
 * access and routing capabilities for digital identity services.
 */
@SpringBootApplication
public class EudiwClientGatewayApplication {

  /**
   * Main method to start the EUDIW Client Gateway application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(EudiwClientGatewayApplication.class, args);
  }
}
