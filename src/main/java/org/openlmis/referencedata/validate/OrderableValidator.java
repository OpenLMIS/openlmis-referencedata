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

import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_NET_CONTENT_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_NULL;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_PACK_ROUNDING_THRESHOLD_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_PRODUCT_CODE_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys.ERROR_ROUND_TO_ZERO_REQUIRED;

import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.dto.TemperatureMeasurementDto;
import org.openlmis.referencedata.dto.VolumeMeasurementDto;
import org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * A validator for {@link OrderableDto} object.
 */
@Component
public class OrderableValidator implements BaseValidator {

  static final String MINIMUM_TOLERANCE_TEMPERATURE = "minimumToleranceTemperature";
  static final String MAXIMUM_TOLERANCE_TEMPERATURE = "maximumToleranceTemperature";
  static final String IN_BOX_CUBE_DIMENSION = "inBoxCubeDimension";

  /**
   * Checks if the given class definition is supported.
   *
   * @param clazz the {@link Class} that this {@link Validator} is being asked if it can {@link
   *              #validate(Object, Errors) validate}
   * @return true if {@code clazz} is equal to {@link OrderableDto} class definition.
   *     Otherwise false.
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return OrderableDto.class.equals(clazz);
  }

  /**
   * Validates the {@code target} object, which must be an instance of {@link OrderableDto} class.
   *
   * @param target the object that is to be validated (never {@code null})
   * @param errors contextual state about the validation process (never {@code null})
   * @see ValidationUtils
   */
  @Override
  public void validate(Object target, Errors errors) {
    verifyArguments(target, errors, ERROR_NULL);

    rejectIfEmptyOrWhitespace(errors, "productCode",ERROR_PRODUCT_CODE_REQUIRED);
    rejectIfNull(errors, "packRoundingThreshold", ERROR_PACK_ROUNDING_THRESHOLD_REQUIRED);
    rejectIfNull(errors, "roundToZero", ERROR_ROUND_TO_ZERO_REQUIRED);
    rejectIfNull(errors, "netContent", ERROR_NET_CONTENT_REQUIRED);

    OrderableDto dto = (OrderableDto) target;
    validateToleranceTemperature(dto, errors);
    validateVolumeMeasurement(dto, errors);

  }

  private void validateToleranceTemperature(OrderableDto dto, Errors errors) {
    validateMinimumToleranceTemperature(dto.getMinimumToleranceTemperature(), errors);
    validateMaximumToleranceTemperature(dto.getMaximumToleranceTemperature(), errors);

    if (isMinTemperatureValueGreaterThanMaxTemperatureValue(
            dto.getMinimumToleranceTemperature(),
            dto.getMaximumToleranceTemperature())) {
      rejectValue(errors, MINIMUM_TOLERANCE_TEMPERATURE, OrderableMessageKeys
              .ERROR_MINIMUM_TOLERANCE_TEMPERATURE_VALUE);
    }
  }

  private void validateMinimumToleranceTemperature(
          TemperatureMeasurementDto minimumToleranceTemperature, Errors errors) {
    if (isNotToleranceTemperatureCodeSupported(minimumToleranceTemperature)) {
      rejectValue(errors, MINIMUM_TOLERANCE_TEMPERATURE, OrderableMessageKeys
              .ERROR_MINIMUM_TOLERANCE_TEMPERATURE_UNIT_CODE_NOT_SUPPORTED);
    }

    if (isNotGivenToleranceTemperatureCode(minimumToleranceTemperature)) {
      rejectValue(errors, MINIMUM_TOLERANCE_TEMPERATURE, OrderableMessageKeys
              .ERROR_MINIMUM_TOLERANCE_TEMPERATURE_UNIT_CODE_REQUIRED);
    }

    if (isNotGivenToleranceTemperatureValue(minimumToleranceTemperature)) {
      rejectValue(errors, MINIMUM_TOLERANCE_TEMPERATURE, OrderableMessageKeys
              .ERROR_MINIMUM_TOLERANCE_TEMPERATURE_VALUE_REQUIRED);
    }
  }

  private void validateMaximumToleranceTemperature(
          TemperatureMeasurementDto maximumToleranceTemperature, Errors errors) {
    if (isNotToleranceTemperatureCodeSupported(maximumToleranceTemperature)) {
      rejectValue(errors, MAXIMUM_TOLERANCE_TEMPERATURE, OrderableMessageKeys
              .ERROR_MAXIMUM_TOLERANCE_TEMPERATURE_UNIT_CODE_NOT_SUPPORTED);
    }

    if (isNotGivenToleranceTemperatureCode(maximumToleranceTemperature)) {
      rejectValue(errors, MAXIMUM_TOLERANCE_TEMPERATURE, OrderableMessageKeys
              .ERROR_MAXIMUM_TOLERANCE_TEMPERATURE_UNIT_CODE_REQUIRED);
    }

    if (isNotGivenToleranceTemperatureValue(maximumToleranceTemperature)) {
      rejectValue(errors, MAXIMUM_TOLERANCE_TEMPERATURE, OrderableMessageKeys
              .ERROR_MAXIMUM_TOLERANCE_TEMPERATURE_VALUE_REQUIRED);
    }
  }

  private void validateVolumeMeasurement(OrderableDto dto, Errors errors) {
    if (isGivenInBoxCubeDimension(dto.getInBoxCubeDimension())) {
      if (isNotMeasurementUnitCodeSupported(dto.getInBoxCubeDimension())) {
        rejectValue(errors, IN_BOX_CUBE_DIMENSION, OrderableMessageKeys
                .ERROR_IN_BOX_CUBE_DIMENSION_UNIT_CODE_NOT_SUPPORTED);
      }
      if (!isMeasurementVolumeValuePositive(dto.getInBoxCubeDimension().getValue())) {
        rejectValue(errors, IN_BOX_CUBE_DIMENSION, OrderableMessageKeys
                .ERROR_IN_BOX_CUBE_DIMENSION_VALUE);
      }
    }
    if (isNotGivenInBoxCubeDimensionCode(dto.getInBoxCubeDimension())) {
      rejectValue(errors, IN_BOX_CUBE_DIMENSION, OrderableMessageKeys
              .ERROR_IN_BOX_CUBE_DIMENSION_UNIT_CODE_REQUIRED);
    }
    if (isNotGivenInBoxCubeDimensionValue(dto.getInBoxCubeDimension())) {
      rejectValue(errors, IN_BOX_CUBE_DIMENSION, OrderableMessageKeys
              .ERROR_IN_BOX_CUBE_DIMENSION_VALUE_REQUIRED);
    }
  }

  private boolean isNotToleranceTemperatureCodeSupported(
          TemperatureMeasurementDto temperatureMeasurement) {
    return isGivenToleranceTemperature(temperatureMeasurement)
            && !temperatureMeasurement.getCodeListVersion()
            .contains(temperatureMeasurement.getTemperatureMeasurementUnitCode());
  }

  private boolean isNotMeasurementUnitCodeSupported(VolumeMeasurementDto volumeMeasurement) {
    return !volumeMeasurement.getCodeListVersion()
            .contains(volumeMeasurement.getMeasurementUnitCode());
  }

  private boolean isMinTemperatureValueGreaterThanMaxTemperatureValue(
          TemperatureMeasurementDto minTemperature, TemperatureMeasurementDto maxTemperature) {
    return isGivenToleranceTemperature(minTemperature)
            && isGivenToleranceTemperature(maxTemperature)
            && minTemperature.getValue() > maxTemperature.getValue();
  }

  private boolean isMeasurementVolumeValuePositive(Double value) {
    return value > 0;
  }

  private boolean isGivenToleranceTemperature(TemperatureMeasurementDto temperatureMeasurement) {
    return temperatureMeasurement != null
            && temperatureMeasurement.getTemperatureMeasurementUnitCode() != null
            && temperatureMeasurement.getValue() != null;
  }

  private boolean isNotGivenToleranceTemperatureCode(
          TemperatureMeasurementDto temperatureMeasurement) {
    return temperatureMeasurement != null
            && temperatureMeasurement.getTemperatureMeasurementUnitCode() == null
            && temperatureMeasurement.getValue() != null;
  }

  private boolean isNotGivenToleranceTemperatureValue(
          TemperatureMeasurementDto temperatureMeasurement) {
    return temperatureMeasurement != null
            && temperatureMeasurement.getTemperatureMeasurementUnitCode() != null
            && temperatureMeasurement.getValue() == null;
  }

  private boolean isGivenInBoxCubeDimension(VolumeMeasurementDto volumeMeasurement) {
    return volumeMeasurement != null
            && volumeMeasurement.getMeasurementUnitCode() != null
            && volumeMeasurement.getValue() != null;
  }

  private boolean isNotGivenInBoxCubeDimensionCode(VolumeMeasurementDto volumeMeasurement) {
    return volumeMeasurement != null
            && volumeMeasurement.getMeasurementUnitCode() == null
            && volumeMeasurement.getValue() != null;
  }

  private boolean isNotGivenInBoxCubeDimensionValue(VolumeMeasurementDto volumeMeasurement) {
    return volumeMeasurement != null
            && volumeMeasurement.getMeasurementUnitCode() != null
            && volumeMeasurement.getValue() == null;
  }
}
