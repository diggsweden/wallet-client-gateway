// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.attestation.client;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.ApplicationConfig.Attributeattestation;
import se.digg.wallet.gateway.infrastructure.attestation.model.ClientAttestationDto;
import se.digg.wallet.gateway.infrastructure.attestation.model.ClientAttestationListDto;

@Component
public class AttestationsClient {
  private RestClient restClient;
  private Attributeattestation attestationConfig;
  private Logger log = LoggerFactory.getLogger(AttestationsClient.class);

  public AttestationsClient(RestClient restClient, ApplicationConfig applicationConfig) {
    this.restClient = restClient;
    this.attestationConfig = applicationConfig.attributeattestation();
  }

  public Optional<ClientAttestationDto> creatAttestation(ClientAttestationDto attribute) {
    try {
      return Optional.of(restClient
          .post()
          .uri(attestationConfig.baseurl() + attestationConfig.paths().post())
          .body(attribute)
          .contentType(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(ClientAttestationDto.class));
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
        return Optional.empty();
      } else {
        log.error("Unable to connnect to server backend", e);
        throw e;
      }
    }
  }

  public Optional<ClientAttestationDto> getAttestation(UUID id) {
    try {
      return Optional.of(restClient
          .get()
          .uri(attestationConfig.baseurl() + attestationConfig.paths().getById() + "/"
              + id.toString())
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(ClientAttestationDto.class));
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      } else {
        log.error("Unable to connnect to server backend", e);
        throw e;
      }
    }
  }

  public ClientAttestationListDto getAttestationByHsmId(UUID hsmId) {
    return restClient
        .get()
        .uri(attestationConfig.baseurl() + attestationConfig.paths().getByKey() + "/"
            + hsmId.toString())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(ClientAttestationListDto.class);
  }


}
