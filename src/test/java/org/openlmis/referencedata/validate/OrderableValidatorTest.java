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
import static org.openlmis.referencedata.validate.OrderableValidator.IN_BOX_CUBE_DIMENSION;
import static org.openlmis.referencedata.validate.OrderableValidator.MAXIMUM_TOLERANCE_TEMPERATURE;
import static org.openlmis.referencedata.validate.OrderableValidator.MINIMUM_TOLERANCE_TEMPERATURE;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.measurement.TemperatureMeasurement;
import org.openlmis.referencedata.domain.measurement.VolumeMeasurement;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class OrderableValidatorTest {

  @InjectMocks
  private Validator validator = new OrderableValidator();

  private Orderable orderable;
  private OrderableDto orderableDto;
  private Errors errors;
  private static final String CEl = "CEL";

  @Before
  public void setUp() {
    orderableDto = new OrderableDto();

    orderable = new OrderableDataBuilder().build();
    orderable.export(orderableDto);

    errors = new BeanPropertyBindingResult(orderableDto, "orderableDto");
  }

  @Test
  public void shouldNotFindErrorsWhenOrderableIsValid() throws Exception {
    validator.validate(orderableDto, errors);

    assertThat(errors.getErrorCount()).isEqualTo(0);
  }

  @Test
  public void shouldNotRejectIfToleranceTemperatureUnitCodeIsSupported() {
    TemperatureMeasurement minimumToleranceTemperature =
            new TemperatureMeasurement(Double.valueOf(2), CEl);
    orderableDto.setMinimumToleranceTemperature(minimumToleranceTemperature);

    TemperatureMeasurement maximumToleranceTemperature =
            new TemperatureMeasurement(Double.valueOf(8), CEl);
    orderableDto.setMaximumToleranceTemperature(maximumToleranceTemperature);

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(MINIMUM_TOLERANCE_TEMPERATURE)).isFalse();
    assertThat(errors.hasFieldErrors(MAXIMUM_TOLERANCE_TEMPERATURE)).isFalse();
  }

  @Test
  public void shouldRejectIfToleranceTemperatureUnitCodeIsNotSupported() {
    TemperatureMeasurement minimumToleranceTemperature =
            new TemperatureMeasurement(Double.valueOf(2), "F");
    orderableDto.setMinimumToleranceTemperature(minimumToleranceTemperature);

    TemperatureMeasurement maximumToleranceTemperature =
            new TemperatureMeasurement(Double.valueOf(8), "F");
    orderableDto.setMaximumToleranceTemperature(maximumToleranceTemperature);

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(MINIMUM_TOLERANCE_TEMPERATURE)).isTrue();
    assertThat(errors.hasFieldErrors(MAXIMUM_TOLERANCE_TEMPERATURE)).isTrue();
  }

  @Test
  public void shouldRejectIfMinTemperatureValueIsGreaterThanMaxTemperatureValue() {
    TemperatureMeasurement minimumToleranceTemperature =
            new TemperatureMeasurement(Double.valueOf(12), CEl);
    orderableDto.setMinimumToleranceTemperature(minimumToleranceTemperature);

    TemperatureMeasurement maximumToleranceTemperature =
            new TemperatureMeasurement(Double.valueOf(8), CEl);
    orderableDto.setMaximumToleranceTemperature(maximumToleranceTemperature);

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(MINIMUM_TOLERANCE_TEMPERATURE)).isTrue();
  }

  @Test
  public void shouldRejectIfInBoxCubeDimensionCodeIsNotSupported() {
    VolumeMeasurement volumeMeasurement =
            new VolumeMeasurement(Double.valueOf(2), "ML");
    orderableDto.setInBoxCubeDimension(volumeMeasurement);

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(IN_BOX_CUBE_DIMENSION)).isTrue();
  }

  @Test
  public void shouldRejectIfInBoxCubeDimensionValueIsNotPositiveNumber() {
    VolumeMeasurement volumeMeasurement =
            new VolumeMeasurement(Double.valueOf(-0.2), "LTR");
    orderableDto.setInBoxCubeDimension(volumeMeasurement);

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(IN_BOX_CUBE_DIMENSION)).isTrue();
  }

  @Test
  public void shouldRejectIfInBoxCubeDimensionCodeIsNotGiven() {
    VolumeMeasurement volumeMeasurement =
            new VolumeMeasurement(Double.valueOf(2), null);
    orderableDto.setInBoxCubeDimension(volumeMeasurement);

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(IN_BOX_CUBE_DIMENSION)).isTrue();
  }

  @Test
  public void shouldRejectIfToleranceTemperatureUnitCodeIsNotGiven() {
    TemperatureMeasurement minimumToleranceTemperature =
            new TemperatureMeasurement(Double.valueOf(2), null);
    orderableDto.setMinimumToleranceTemperature(minimumToleranceTemperature);

    TemperatureMeasurement maximumToleranceTemperature =
            new TemperatureMeasurement(Double.valueOf(8), null);
    orderableDto.setMaximumToleranceTemperature(maximumToleranceTemperature);

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(MINIMUM_TOLERANCE_TEMPERATURE)).isTrue();
    assertThat(errors.hasFieldErrors(MAXIMUM_TOLERANCE_TEMPERATURE)).isTrue();
  }
}
