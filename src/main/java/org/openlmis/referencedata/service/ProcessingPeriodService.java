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
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ProcessingPeriodService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingPeriodService.class);

  @Autowired
  private ProcessingPeriodRepository periodRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private ProcessingScheduleRepository processingScheduleRepository;

  @Autowired
  private RequisitionGroupProgramScheduleRepository repository;

  /**
   * Finds Periods matching all of provided parameters.
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
   * Finds all ProcessingPeriods matching all of provided parameters.
   */
  public List<ProcessingPeriod> searchPeriods(ProcessingPeriodSearchParams params) {
    params.validate();

    Program program = getById(programRepository, params.getProgramId());
    Facility facility = getById(facilityRepository, params.getFacilityId());
    ProcessingSchedule schedule = null;

    if (null != program && null != facility) {
      List<RequisitionGroupProgramSchedule> schedules = repository
          .searchRequisitionGroupProgramSchedules(program, facility);

      if (schedules.isEmpty()) {
        LOGGER.warn("Cannot find Requisition Group Program Schedule for program {} and facility {}",
            program.getId(), facility.getId());
      } else {
        schedule = schedules.get(0).getProcessingSchedule();
      }
    } else {
      schedule = getById(processingScheduleRepository, params.getProcessingScheduleId());
    }

    return searchPeriods(schedule, params.getStartDate());
  }

  private <T> T getById(CrudRepository<T, UUID> repository, UUID id) {
    return null == id ? null : repository.findOne(id);
  }
}
