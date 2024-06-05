/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.referencedata.validate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.DISPLAY_ORDER;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.FACTOR;
import static org.openlmis.referencedata.util.messagekeys.UnitOfOrderableMessageKeys.NAME;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.UnitOfOrderable;
import org.openlmis.referencedata.dto.UnitOfOrderableDto;
import org.openlmis.referencedata.testbuilder.UnitOfOrderableBuilder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class UnitOfOrderableValidatorTest {

  private UnitOfOrderableValidator validator = new UnitOfOrderableValidator();

  private UnitOfOrderableDto unitOfOrderableDto;
  private Errors errors;

  @Before
  public void setUp() {

    UnitOfOrderable unitOfOrderable = new UnitOfOrderableBuilder()
        .withName("testName")
        .withDescription("sample description")
        .withDisplayOrder(0)
        .withFactor(10)
        .build();
    unitOfOrderableDto = UnitOfOrderableDto.newInstance(unitOfOrderable);

    errors = new BeanPropertyBindingResult(unitOfOrderableDto, "unitOfOrderableDto");
  }

  @Test
  public void shouldNotFindErrorsWhenUnitOfOrderableIsValid() {
    //given
    //when
    validator.validate(unitOfOrderableDto, errors);

    //then
    assertThat(errors.getErrorCount()).isZero();
  }

  @Test
  public void shouldNotRejectIfDisplayOrderAndFactorArePositiveOrZero() {
    //given
    //when
    validator.validate(unitOfOrderableDto, errors);

    //then
    assertThat(errors.hasFieldErrors(DISPLAY_ORDER)).isFalse();
    assertThat(errors.hasFieldErrors(FACTOR)).isFalse();
  }

  @Test
  public void shouldRejectIfNameIsNotGiven() {
    //given
    unitOfOrderableDto.setName(null);

    //when
    validator.validate(unitOfOrderableDto, errors);

    //then
    assertThat(errors.hasFieldErrors(NAME)).isTrue();
  }

  @Test
  public void shouldRejectIfDisplayOrderIsNegative() {
    //given
    unitOfOrderableDto.setDisplayOrder(-1);

    //when
    validator.validate(unitOfOrderableDto, errors);

    //then
    assertThat(errors.hasFieldErrors(DISPLAY_ORDER)).isTrue();
  }

  @Test
  public void shouldRejectIfFactorIsNegative() {
    //given
    unitOfOrderableDto.setFactor(-1);

    //when
    validator.validate(unitOfOrderableDto, errors);

    //then
    assertThat(errors.hasFieldErrors(FACTOR)).isTrue();
  }
}
