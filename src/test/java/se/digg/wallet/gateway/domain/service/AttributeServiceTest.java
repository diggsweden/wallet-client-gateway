// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2
package se.digg.wallet.gateway.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import se.digg.wallet.gateway.application.model.AttributeDto;
import se.digg.wallet.gateway.application.model.CreateAttributeDto;
import se.digg.wallet.gateway.infrastructure.downstream.client.DownstreamServiceClient;
import se.digg.wallet.gateway.infrastructure.downstream.model.DownstreamAttributeDto;

class AttributeServiceTest {

  public static final String TEST_ATTRIBUTE_VALUE = "test attribute value";
  public static final String TEST_ATTRIBUTE_ID = "12345";

  @Mock
  private DownstreamServiceClient client;

  private AttributeService attributeService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    attributeService = new AttributeService(client);
  }

  @Test
  void createAttribute_Success() {
    // Given
    CreateAttributeDto createAttributeDto = new CreateAttributeDto(TEST_ATTRIBUTE_VALUE);
    AttributeDto expectedAttributeDto = new AttributeDto(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE);
    when(client.createAttribute(any()))
        .thenReturn(new DownstreamAttributeDto(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE));

    // When
    AttributeDto actualAttributeDto = attributeService.createAttribute(createAttributeDto);

    // Then
    assertEquals(expectedAttributeDto, actualAttributeDto);
  }

  @Test
  void getAttribute_Success() {
    // Given
    AttributeDto expectedAttributeDto = new AttributeDto(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE);

    when(client.getAttribute(TEST_ATTRIBUTE_ID))
        .thenReturn(new DownstreamAttributeDto(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE));
    // When
    AttributeDto actualAttributeDto = attributeService.getAttribute(TEST_ATTRIBUTE_ID);

    // Then
    assertEquals(expectedAttributeDto, actualAttributeDto);
  }

  @Test
  void getAttribute_NotFound() {
    when(client.getAttribute(anyString()))
        .thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404)));

    // When & Then
    assertThrows(
        HttpClientErrorException.class, () -> attributeService.getAttribute(TEST_ATTRIBUTE_ID));
  }
}
