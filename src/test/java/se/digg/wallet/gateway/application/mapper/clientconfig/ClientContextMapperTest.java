// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.mapper.clientconfig;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import se.digg.wallet.gateway.domain.model.clientconfig.ClientContext;

class ClientContextMapperTest {

  @Test
  void mapsWalletHeadersToClientContext() {
    var request = new MockHttpServletRequest();
    request.addHeader(ClientContextMapper.DEVICE_OS, "Android");
    request.addHeader(ClientContextMapper.DEVICE_OS_VERSION, "15");
    request.addHeader(ClientContextMapper.DEVICE_MODEL, "Pixel 9");
    request.addHeader(ClientContextMapper.APP_VERSION, "2.1.0");

    assertThat(ClientContextMapper.from(request))
        .isEqualTo(new ClientContext("Android", "15", "Pixel 9", "2.1.0"));
  }
}
