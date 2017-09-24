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

import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class FacilityValidator implements BaseValidator {

  protected static final String CODE = "code";

  @Autowired
  private FacilityRepository facilityRepository;

  @Override
  public boolean supports(Class<?> clazz) {
    return FacilityDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    verifyArguments(target, errors, FacilityMessageKeys.ERROR_NULL);
    rejectIfEmptyOrWhitespace(errors, CODE, FacilityMessageKeys.ERROR_CODE_REQUIRED);

    FacilityDto facilityDto = (FacilityDto) target;
    if (facilityRepository.existsByCode(facilityDto.getCode())
        && (facilityDto.getId() == null
            || !facilityRepository.findFirstByCode(facilityDto.getCode()).getId()
                .equals(facilityDto.getId()))) {
      rejectValue(errors, CODE, FacilityMessageKeys.ERROR_CODE_MUST_BE_UNIQUE);
    }
  }
}
