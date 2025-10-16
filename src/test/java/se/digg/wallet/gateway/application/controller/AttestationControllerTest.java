// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.controller;


import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;
import se.digg.wallet.gateway.application.model.attestation.AttestationDto;
import se.digg.wallet.gateway.application.model.attestation.AttestationListDto;
import se.digg.wallet.gateway.application.model.attestation.CreateAttestationDto;
import se.digg.wallet.gateway.domain.service.attestation.AttestationService;
import se.digg.wallet.gateway.infrastructure.attestation.client.AttestationsClient;

@WebMvcTest(AttestationController.class)
class AttestationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AttestationsClient attestationsClient;

  @MockitoBean
  private AttestationService attestationService;

  private final UUID defaultUuid = UUID.randomUUID();
  private AttestationDto attestationDto =
      new AttestationDto(defaultUuid, defaultUuid, defaultUuid, "a string");

  @Test
  @WithMockUser
  void testCreateAttestation() throws Exception {
    when(attestationService.createAttestation(any(CreateAttestationDto.class)))
        .thenReturn(Optional.of(attestationDto));
    mockMvc
        .perform(
            post("/attribute-attestations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper
                    .writeValueAsString(
                        attestationDto))
                .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(content().string(containsString("a string")))
        .andExpect(content()
            .string(containsString(defaultUuid.toString())));
  }

  @Test
  @WithMockUser
  void testGetAttestation() throws Exception {
    when(attestationService.getAttestation(defaultUuid))
        .thenReturn(Optional.of(attestationDto));
    mockMvc
        .perform(
            get("/attribute-attestations/" + defaultUuid))
        .andExpect(status().isOk())
        .andExpect(content()
            .string(containsString(defaultUuid.toString())));
  }

  @Test
  @WithMockUser
  void testGetListOfAttestation() throws Exception {

    AttestationListDto attestationListDto =
        new AttestationListDto(List.of(attestationDto), defaultUuid);
    when(attestationService.getAttestationByHsmId(defaultUuid))
        .thenReturn(attestationListDto);
    mockMvc
        .perform(
            get("/attribute-attestations")
                .queryParams(MultiValueMap.fromSingleValue(Map.of("key", defaultUuid.toString()))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.attestations").exists())
        .andExpect(content()
            .string(containsString(defaultUuid.toString())));
  }
}
