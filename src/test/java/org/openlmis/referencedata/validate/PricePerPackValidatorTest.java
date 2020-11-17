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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.testbuilder.PricePerPackBuilder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PricePerPackValidatorTest {

  @InjectMocks
  private Validator validator = new OrderableValidator();

  private OrderableDto orderableDto;
  private Errors errors;

  @Before
  public void setUp() {
    orderableDto = new OrderableDto();
    errors = new BeanPropertyBindingResult(orderableDto, "orderableDto");
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfPricePerPackIsNegative() {
    orderableDto.setPrograms(new PricePerPackBuilder().build());
    validator.validate(orderableDto,errors);
  }
}
