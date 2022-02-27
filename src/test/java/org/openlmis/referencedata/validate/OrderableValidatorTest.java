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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.validate.OrderableValidator.IN_BOX_CUBE_DIMENSION;
import static org.openlmis.referencedata.validate.OrderableValidator.MAXIMUM_TEMPERATURE;
import static org.openlmis.referencedata.validate.OrderableValidator.MINIMUM_TEMPERATURE;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.dto.ProgramOrderableDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.TemperatureMeasurementDataBuilder;
import org.openlmis.referencedata.testbuilder.VolumeMeasurementDataBuilder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@SuppressWarnings({"PMD.TooManyMethods"})
public class OrderableValidatorTest {

  @Mock
  private OrderableRepository orderableRepository;

  @InjectMocks
  private OrderableValidator validator;

  private Orderable orderable;
  private OrderableDto orderableDto;
  private Errors errors;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    orderableDto = new OrderableDto();

    orderable = new OrderableDataBuilder()
            .withProductCode(Code.code("PRODUCT_CODE"))
            .withMinimumTemperature("CEL", 2.0)
            .withMaximumTemperature("CEL", 8.0)
            .withInBoxCubeDimension("MLT", 200.0)
            .build();
    orderable.export(orderableDto);

    when(orderableRepository.findFirstByVersionNumberAndProductCodeIgnoreCase(any(), any()))
        .thenReturn(null);

    errors = new BeanPropertyBindingResult(orderableDto, "orderableDto");
  }

  @Test
  public void shouldNotFindErrorsWhenOrderableIsValid() {
    validator.validate(orderableDto, errors);

    assertThat(errors.getErrorCount()).isEqualTo(0);
  }

  @Test
  public void shouldNotRejectIfTemperatureUnitCodeIsSupported() {
    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(MINIMUM_TEMPERATURE)).isFalse();
    assertThat(errors.hasFieldErrors(MAXIMUM_TEMPERATURE)).isFalse();
  }

  @Test
  public void shouldRejectIfTemperatureUnitCodeIsNotSupported() {
    orderableDto.setMinimumTemperature(new TemperatureMeasurementDataBuilder()
            .withTemperatureMeasurementUnitCode("F").build());
    orderableDto.setMaximumTemperature(new TemperatureMeasurementDataBuilder()
            .withTemperatureMeasurementUnitCode("F").build());

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(MINIMUM_TEMPERATURE)).isTrue();
    assertThat(errors.hasFieldErrors(MAXIMUM_TEMPERATURE)).isTrue();
  }

  @Test
  public void shouldRejectIfMinTemperatureValueIsGreaterThanMaxTemperatureValue() {
    orderableDto.setMinimumTemperature(new TemperatureMeasurementDataBuilder()
            .withValue(12.0).build());

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(MINIMUM_TEMPERATURE)).isTrue();
  }

  @Test
  public void shouldRejectIfInBoxCubeDimensionCodeIsNotSupported() {
    orderableDto.setInBoxCubeDimension(new VolumeMeasurementDataBuilder()
            .withMeasurementUnitCode("ML").build());

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(IN_BOX_CUBE_DIMENSION)).isTrue();
  }

  @Test
  public void shouldRejectIfInBoxCubeDimensionValueIsNotPositiveNumber() {
    orderableDto.setInBoxCubeDimension(new VolumeMeasurementDataBuilder()
            .withValue(-0.2).build());

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(IN_BOX_CUBE_DIMENSION)).isTrue();
  }

  @Test
  public void shouldRejectIfInBoxCubeDimensionCodeIsNotGiven() {
    orderableDto.setInBoxCubeDimension(new VolumeMeasurementDataBuilder()
            .withMeasurementUnitCode(null).build());

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(IN_BOX_CUBE_DIMENSION)).isTrue();
  }

  @Test
  public void shouldRejectIfInBoxCubeDimensionValueIsNotGiven() {
    orderableDto.setInBoxCubeDimension(new VolumeMeasurementDataBuilder()
            .withValue(null).build());

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(IN_BOX_CUBE_DIMENSION)).isTrue();
  }

  @Test
  public void shouldRejectIfTemperatureUnitCodeIsNotGiven() {
    orderableDto.setMinimumTemperature(new TemperatureMeasurementDataBuilder()
            .withTemperatureMeasurementUnitCode(null).build());
    orderableDto.setMaximumTemperature(new TemperatureMeasurementDataBuilder()
            .withTemperatureMeasurementUnitCode(null).build());

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(MINIMUM_TEMPERATURE)).isTrue();
    assertThat(errors.hasFieldErrors(MAXIMUM_TEMPERATURE)).isTrue();
  }

  @Test
  public void shouldRejectIfOnlyTemperatureUnitCodeIsNotGiven() {
    orderableDto.setMinimumTemperature(new TemperatureMeasurementDataBuilder()
            .withValue(null).build());
    orderableDto.setMaximumTemperature(new TemperatureMeasurementDataBuilder()
            .withValue(null).build());

    validator.validate(orderableDto, errors);
    assertThat(errors.hasFieldErrors(MINIMUM_TEMPERATURE)).isTrue();
    assertThat(errors.hasFieldErrors(MAXIMUM_TEMPERATURE)).isTrue();
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfPricePerPackIsNegative() {
    ProgramOrderableDto programOrderableDto = new ProgramOrderableDto();
    Money pricePerPack = Money.parse("USD -23.87");
    programOrderableDto.setPricePerPack(pricePerPack);
    Set<ProgramOrderableDto> programs = new HashSet<>();
    programs.add(programOrderableDto);
    orderableDto.setPrograms(programs);
    validator.validate(orderableDto,errors);
  }

  @Test
  public void shouldNotNotThrowExceptionWhenProgramsIsNullAndOrderableIsValid() {
    orderableDto.setPrograms(null);

    validator.validate(orderableDto, errors);

    assertThat(errors.getErrorCount()).isEqualTo(0);
  }

  @Test
  public void shouldRejectIfProductCodeIsNotUnique() {
    orderableDto.setId(null);

    Orderable repositoryOrderable = new OrderableDataBuilder()
            .withProductCode(Code.code(orderableDto.getProductCode().toLowerCase()))
            .build();

    when(orderableRepository.findFirstByVersionNumberAndProductCodeIgnoreCase(
        orderableDto.getProductCode(),
        orderableDto.getVersionNumber()
    ))
            .thenReturn(repositoryOrderable);

    validator.validate(orderableDto, errors);

    assertThat(errors.getErrorCount()).isEqualTo(1);
  }

  @Test
  public void shouldNotRejectIfProductCodeIsNotUniqueForOrderableMatchingVersionIdentity() {
    UUID versionIdentityId = UUID.fromString("7f7b83db-580d-4269-88d2-7f9c80a591a9");

    orderableDto.setId(versionIdentityId);

    Orderable repositoryOrderable = new OrderableDataBuilder()
        .withId(versionIdentityId)
        .withVersionNumber(orderableDto.getVersionNumber())
        .withProductCode(Code.code(orderableDto.getProductCode().toLowerCase()))
        .build();

    when(orderableRepository.findFirstByVersionNumberAndProductCodeIgnoreCase(
        orderableDto.getProductCode(),
        orderableDto.getVersionNumber()
    ))
        .thenReturn(repositoryOrderable);

    validator.validate(orderableDto, errors);

    assertThat(errors.getErrorCount()).isEqualTo(0);
  }

  @Test
  public void shouldReject() {
    orderableDto.setId(UUID.fromString("7f7b83db-580d-4269-88d2-7f9c80a591a9"));

    Orderable repositoryOrderable = new OrderableDataBuilder()
        .withId(UUID.fromString("b659c6aa-37ad-4cb9-bf76-7e6669c04fed"))
        .withVersionNumber(orderableDto.getVersionNumber())
        .withProductCode(Code.code(orderableDto.getProductCode().toLowerCase()))
        .build();

    when(orderableRepository.findFirstByVersionNumberAndProductCodeIgnoreCase(
        orderableDto.getProductCode(),
        orderableDto.getVersionNumber()
    ))
        .thenReturn(repositoryOrderable);

    validator.validate(orderableDto, errors);

    assertThat(errors.getErrorCount()).isEqualTo(1);
  }
}
