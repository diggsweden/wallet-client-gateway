// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.model;

import static org.junit.Assert.assertEquals;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import se.digg.wallet.gateway.application.model.wua.CreateWuaDto;
import se.digg.wallet.gateway.application.model.wua.JwkDto;

public class ValidationTest {

  private Validator validator;

  @Before
  public void setUp() {
    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @Test
  public void validateCreateWuaTest() {
    CreateWuaDto okWuaDto = new CreateWuaDto(UUID.randomUUID(),
        new JwkDto("kty", null, "null", "null", "null", "null", "null"));
    CreateWuaDto inValidWuaDto = new CreateWuaDto(UUID.randomUUID(),
        new JwkDto("", null, "null", "null", "null", "null", "null"));
    var okResult = validator.validate(okWuaDto);
    var invalidResult = validator.validate(inValidWuaDto);
    assertEquals(0, okResult.size());
    assertEquals(1, invalidResult.size());

  }

}
