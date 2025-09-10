// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2
package se.digg.wallet.gateway.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.model.AttributeDto;
import se.digg.wallet.gateway.application.model.CreateAttributeDto;

class AttributeServiceTest {

  public static final String TEST_ATTRIBUTE_VALUE = "test attribute value";
  public static final String TEST_DOWNSTREAM_SERVICE_URL = "http://localhost:8080/attributes";
  public static final String TEST_ATTRIBUTE_ID = "12345";

  @Mock
  private WebClient webClient;

  @Mock
  private ApplicationConfig applicationConfig;

  private AttributeService attributeService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    attributeService = new AttributeService(webClient, applicationConfig);
  }

  @Test
  void createAttribute_Success() {
    // Given
    CreateAttributeDto createAttributeDto = new CreateAttributeDto();
    createAttributeDto.setValue(TEST_ATTRIBUTE_VALUE);
    AttributeDto expectedAttributeDto = new AttributeDto(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE);

    when(applicationConfig.downstreamServiceUrl()).thenReturn(TEST_DOWNSTREAM_SERVICE_URL);

    WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
    WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
    WebClient.RequestHeadersSpec<WebClient.RequestBodySpec> requestHeadersSpec =
        mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    doReturn(requestBodyUriSpec).when(webClient).post();
    doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
    doReturn(requestHeadersSpec).when(requestBodySpec).bodyValue(any());
    doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    doReturn(Mono.just(expectedAttributeDto)).when(responseSpec).bodyToMono(eq(AttributeDto.class));

    // When
    AttributeDto actualAttributeDto = attributeService.createAttribute(createAttributeDto);

    // Then
    assertEquals(expectedAttributeDto, actualAttributeDto);
  }

  @Test
  void getAttribute_Success() {
    // Given
    AttributeDto expectedAttributeDto = new AttributeDto(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE);

    when(applicationConfig.downstreamServiceUrl()).thenReturn(TEST_DOWNSTREAM_SERVICE_URL);

    WebClient.RequestHeadersUriSpec requestHeadersUriSpec =
        mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
    doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
    doReturn(Mono.just(expectedAttributeDto)).when(responseSpec).bodyToMono(eq(AttributeDto.class));

    // When
    AttributeDto actualAttributeDto = attributeService.getAttribute(TEST_ATTRIBUTE_ID);

    // Then
    assertEquals(expectedAttributeDto, actualAttributeDto);
  }

  @Test
  void getAttribute_NotFound() {
    when(applicationConfig.downstreamServiceUrl()).thenReturn(TEST_DOWNSTREAM_SERVICE_URL);

    WebClient.RequestHeadersUriSpec requestHeadersUriSpec =
        mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
    doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
    doReturn(Mono.error(new HttpClientErrorException(HttpStatus.NOT_FOUND)))
        .when(responseSpec)
        .bodyToMono(eq(AttributeDto.class));

    // When & Then
    assertThrows(
        HttpClientErrorException.class, () -> attributeService.getAttribute(TEST_ATTRIBUTE_ID));
  }
}
