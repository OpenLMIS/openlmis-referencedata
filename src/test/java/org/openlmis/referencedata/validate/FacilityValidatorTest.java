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
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.validate.FacilityValidator.CODE;
import static org.openlmis.referencedata.validate.ValidationTestUtils.assertErrorMessage;

@RunWith(MockitoJUnitRunner.class)
public class FacilityValidatorTest {

  @Mock
  private FacilityRepository facilityRepository;

  @InjectMocks
  private Validator validator = new FacilityValidator();

  private Errors errors;
  private FacilityDto facilityDto;

  @Before
  public void setUp() throws Exception {
    facilityDto = new FacilityDto();
    facilityDto.setCode("code");

    errors = new BeanPropertyBindingResult(facilityDto, "facilityDto");

    when(facilityRepository.existsByCode(any(String.class))).thenReturn(false);
  }

  @Test
  public void shouldNotFindErrorsWhenFacilityIsValid() throws Exception {
    validator.validate(facilityDto, errors);

    assertEquals(0, errors.getErrorCount());
  }

  @Test
  public void shouldRejectWhenFacilityCodeIsNull() {
    facilityDto.setCode(null);

    validator.validate(facilityDto, errors);

    assertErrorMessage(errors, CODE, FacilityMessageKeys.ERROR_CODE_REQUIRED);
  }

  @Test
  public void shouldRejectWhenFacilityCodeIsEmpty() {
    facilityDto.setCode("");

    validator.validate(facilityDto, errors);

    assertErrorMessage(errors, CODE, FacilityMessageKeys.ERROR_CODE_REQUIRED);
  }

  @Test
  public void shouldRejectWhenFacilityCodeIsWhitespace() {
    facilityDto.setCode(" ");

    validator.validate(facilityDto, errors);

    assertErrorMessage(errors, CODE, FacilityMessageKeys.ERROR_CODE_REQUIRED);
  }

  @Test
  public void shouldRejectWhenFacilityCodeAlreadyExistAndIdIsEmpty() {
    when(facilityRepository.existsByCode(facilityDto.getCode())).thenReturn(true);
    facilityDto.setId(null);

    validator.validate(facilityDto, errors);

    assertErrorMessage(errors, CODE, FacilityMessageKeys.ERROR_CODE_MUST_BE_UNIQUE);
  }

  @Test
  public void shouldRejectWhenFacilityCodeAlreadyExistAndIdIsDifferentThanFoundOne() {
    Facility foundFacility = new Facility(facilityDto.getCode());
    foundFacility.setId(UUID.randomUUID());
    facilityDto.setId(UUID.randomUUID());

    when(facilityRepository.existsByCode(facilityDto.getCode())).thenReturn(true);
    when(facilityRepository.findFirstByCode(facilityDto.getCode())).thenReturn(foundFacility);

    validator.validate(facilityDto, errors);

    assertErrorMessage(errors, CODE, FacilityMessageKeys.ERROR_CODE_MUST_BE_UNIQUE);
  }
}
