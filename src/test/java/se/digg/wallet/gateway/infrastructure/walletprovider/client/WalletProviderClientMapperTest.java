// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.infrastructure.walletprovider.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.digg.wallet.gateway.domain.exception.WalletRuntimeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.digg.wallet.gateway.domain.common.JwkTestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    WalletProviderClientMapper.class,
    ObjectMapper.class
})
@ActiveProfiles("test")
public class WalletProviderClientMapperTest {

  @MockitoBean
  private ObjectMapper objectMapper;

  @Autowired
  private WalletProviderClientMapper mapper;

  @Test
  void a_null_key_throws_illegal_argument_exception() {

    assertThatThrownBy(() -> mapper.jwkToJsonString(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void json_processing_failure_throws_wallet_runtime_exception() throws JsonProcessingException {


    when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

    assertThatThrownBy(() -> mapper.jwkToJsonString(JwkTestBuilder.withDefaults().build()))
        .isInstanceOf(WalletRuntimeException.class);
  }

  @Test
  void serves_mapped_key() throws JsonProcessingException {

    var jwk = JwkTestBuilder.withDefaults().build();
    when(objectMapper.writeValueAsString(any()))
        .thenReturn("{\"kid\": \"%s\"}".formatted(jwk.kid()));

    var result = mapper.jwkToJsonString(jwk);

    assertThat(result).isNotNull();
    assertThat(result.startsWith("{"));
    assertThat(result.contains(mappedKeyValue("kid", jwk.kid())));
    assertThat(result.endsWith("}"));
  }

  private String mappedKeyValue(String key, String value) {
    return """
            "%s": "%s"
        """.formatted(key, value);
  }
}
