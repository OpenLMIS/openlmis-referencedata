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

package org.openlmis.referencedata.service;

import static org.openlmis.referencedata.util.messagekeys.ProcessingPeriodMessageKeys.ERROR_FACILITY_ID_NULL;
import static org.openlmis.referencedata.util.messagekeys.ProcessingPeriodMessageKeys.ERROR_PROGRAM_ID_NULL;
import static org.openlmis.referencedata.util.messagekeys.ProcessingPeriodMessageKeys.ERROR_SCHEDULE_ID_SINGLE_PARAMETER;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public final class ProcessingPeriodSearchParams {
  private UUID programId;
  private UUID facilityId;
  private UUID processingScheduleId;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate startDate;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate endDate;

  /**
   * Validates if this search params contains a valid parameters.
   */
  public void validate() {
    if (null != programId && null == facilityId) {
      throw new ValidationMessageException(ERROR_FACILITY_ID_NULL);
    }

    if (null == programId && null != facilityId) {
      throw new ValidationMessageException(ERROR_PROGRAM_ID_NULL);
    }

    if (null != programId && null != facilityId && null != processingScheduleId) {
      throw new ValidationMessageException(ERROR_SCHEDULE_ID_SINGLE_PARAMETER);
    }
  }
}
