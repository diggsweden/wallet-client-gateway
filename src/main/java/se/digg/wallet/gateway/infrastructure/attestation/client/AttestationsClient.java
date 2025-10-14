// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.attestation.client;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.config.ApplicationConfig.Attributeattestation;
import se.digg.wallet.gateway.infrastructure.attestation.model.AttestationDto;
import se.digg.wallet.gateway.infrastructure.attestation.model.AttestationListDto;

@Component
public class AttestationsClient {
  private RestClient restClient;
  private Attributeattestation attestationConfig;

  public AttestationsClient(RestClient restClient, ApplicationConfig applicationConfig) {
    this.restClient = restClient;
    this.attestationConfig = applicationConfig.attributeattestation();
  }

  public Optional<AttestationDto> creatAttestation(AttestationDto attribute) {
    try {
      return Optional.of(restClient
          .post()
          .uri(attestationConfig.baseurl() + attestationConfig.paths().post())
          .body(attribute)
          .contentType(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(AttestationDto.class));
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
        return Optional.empty();
      } else {
        throw e;
      }
    }
  }

  public Optional<AttestationDto> getAttestation(UUID id) {
    try {
      return Optional.of(restClient
          .get()
          .uri(attestationConfig.baseurl() + attestationConfig.paths().getById() + "/"
              + id.toString())
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(AttestationDto.class));
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      } else {
        throw e;
      }
    }
  }

  public AttestationListDto getAttestationByHsmId(UUID hsmId) {
    return restClient
        .get()
        .uri(attestationConfig.baseurl() + attestationConfig.paths().getByUser() + "/"
            + hsmId.toString())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(AttestationListDto.class);
  }


}
