// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.digg.wallet.gateway.application.model.attestation.AttestationDto;
import se.digg.wallet.gateway.application.model.attestation.AttestationListDto;
import se.digg.wallet.gateway.application.model.attestation.CreateAttestationDto;
import se.digg.wallet.gateway.domain.service.attestation.AttestationMapper;
import se.digg.wallet.gateway.infrastructure.attestation.model.ClientAttestationDto;
import se.digg.wallet.gateway.infrastructure.attestation.model.ClientAttestationListDto;

@ExtendWith(MockitoExtension.class)
class AttestationMapperTest {

  @Spy
  private ObjectMapper objectMapper;

  @InjectMocks
  AttestationMapper attestationMapper;

  @Test
  void testMapping() {
    UUID id = UUID.randomUUID();
    UUID hsmId = UUID.randomUUID();
    UUID wuaId = UUID.randomUUID();
    String attestationData = "a string";
    CreateAttestationDto createAttestationDto =
        new CreateAttestationDto(hsmId, wuaId, attestationData);
    ClientAttestationDto clientAttestationDto =
        new ClientAttestationDto(id, hsmId, wuaId, attestationData);
    ClientAttestationListDto clientAttestationListDto =
        new ClientAttestationListDto(List.of(clientAttestationDto), hsmId);
    AttestationDto attestationDto = new AttestationDto(id, hsmId, wuaId, attestationData);
    ClientAttestationDto newClientAttestationDto =
        new ClientAttestationDto(null, hsmId, wuaId, attestationData);
    AttestationListDto attestationListDto = new AttestationListDto(List.of(attestationDto), hsmId);

    assertThat(attestationMapper.toAttestationDto(clientAttestationDto)).usingRecursiveAssertion()
        .isEqualTo(attestationDto);

    assertThat(attestationMapper.toClientAttestationDto(createAttestationDto))
        .usingRecursiveAssertion()
        .isEqualTo(newClientAttestationDto);

    assertThat(attestationMapper.toAttestationListDto(clientAttestationListDto))
        .usingRecursiveAssertion()
        .isEqualTo(attestationListDto);


  }

}
