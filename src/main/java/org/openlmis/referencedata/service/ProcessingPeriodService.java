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

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class ProcessingPeriodService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingPeriodService.class);

  @Autowired
  private ProcessingPeriodRepository periodRepository;

  @Autowired
  private RequisitionGroupProgramScheduleRepository repository;

  /**
   * Finds Periods matching all of provided parameters and
   * ordered ascending by their start date.
   *
   * @param processingSchedule processingSchedule of searched Periods.
   * @param toDate             to which day shall Period start.
   * @return list of all Periods matching all of provided parameters.
   */
  public List<ProcessingPeriod> searchPeriods(ProcessingSchedule processingSchedule,
                                              LocalDate toDate) {
    return periodRepository.searchPeriods(processingSchedule, toDate);
  }

  /**
   * Get Processing Periods matching all of provided parameters.
   *
   * @param program  Program of searched period.
   * @param facility Facility of searched period.
   * @return Collection of Processing Periods.
   */
  public List<ProcessingPeriod> filterPeriods(Program program, Facility facility) {
    List<RequisitionGroupProgramSchedule> schedules =
        repository.searchRequisitionGroupProgramSchedules(program, facility);

    if (schedules.isEmpty()) {
      LOGGER.warn("Cannot find Requisition Group Program Schedule for program {} and facility {}",
          program.getId(), facility.getId());
      return Collections.emptyList();
    }

    return searchPeriods(schedules.get(0).getProcessingSchedule(), null);
  }
}
