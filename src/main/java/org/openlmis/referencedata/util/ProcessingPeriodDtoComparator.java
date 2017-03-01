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

package org.openlmis.referencedata.util;

import org.openlmis.referencedata.dto.ProcessingPeriodDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.messagekeys.ProcessingPeriodMessageKeys;

import lombok.AllArgsConstructor;

import java.util.Comparator;

@AllArgsConstructor
public class ProcessingPeriodDtoComparator implements Comparator<ProcessingPeriodDto> {

  private String compareCondition;

  @Override
  public int compare(ProcessingPeriodDto o1, ProcessingPeriodDto o2) {
    if ("startDate".equals(compareCondition)) {
      return o1.getStartDate().compareTo(o2.getStartDate());
    } else {
      throw new ValidationMessageException(new Message(
          ProcessingPeriodMessageKeys.ERROR_INVALID_SORTING_COLUMN, compareCondition));
    }
  }
}
