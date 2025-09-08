package se.digg.wallet.gateway.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import se.digg.wallet.gateway.application.config.ApplicationConfig;
import se.digg.wallet.gateway.application.model.AttributeDto;
import se.digg.wallet.gateway.application.model.CreateAttributeDto;

class AttributeServiceTest {

  public static final String TEST_ATTRIBUTE_VALUE = "test attribute value";
  public static final String TEST_DOWNSTREAM_SERVICE_URL = "http://localhost:8080/attributes";
  public static final String TEST_ATTRIBUTE_ID = "12345";

  @Mock private RestTemplate restTemplate;

  @Mock private ApplicationConfig applicationConfig;

  @InjectMocks private AttributeService attributeService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createAttribute_Success() {
    // Given
    CreateAttributeDto createAttributeDto = new CreateAttributeDto();
    createAttributeDto.setValue(TEST_ATTRIBUTE_VALUE);
    AttributeDto expectedAttributeDto = new AttributeDto(TEST_ATTRIBUTE_ID, TEST_ATTRIBUTE_VALUE);

    when(applicationConfig.downstreamServiceUrl()).thenReturn(TEST_DOWNSTREAM_SERVICE_URL);
    when(restTemplate.postForObject(
            TEST_DOWNSTREAM_SERVICE_URL, createAttributeDto, AttributeDto.class))
        .thenReturn(expectedAttributeDto);

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
    when(restTemplate.getForObject(
            TEST_DOWNSTREAM_SERVICE_URL + "/" + TEST_ATTRIBUTE_ID, AttributeDto.class))
        .thenReturn(expectedAttributeDto);

    // When
    AttributeDto actualAttributeDto = attributeService.getAttribute(TEST_ATTRIBUTE_ID);

    // Then
    assertEquals(expectedAttributeDto, actualAttributeDto);
  }

  @Test
  void getAttribute_NotFound() {
    when(applicationConfig.downstreamServiceUrl()).thenReturn(TEST_DOWNSTREAM_SERVICE_URL);
    when(restTemplate.getForObject(
            TEST_DOWNSTREAM_SERVICE_URL + "/" + TEST_ATTRIBUTE_ID, AttributeDto.class))
        .thenThrow(HttpClientErrorException.NotFound.class);

    // When & Then
    assertThrows(
        HttpClientErrorException.NotFound.class,
        () -> attributeService.getAttribute(TEST_ATTRIBUTE_ID));
  }
}
