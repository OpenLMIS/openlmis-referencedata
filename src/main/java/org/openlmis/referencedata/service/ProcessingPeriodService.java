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
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProcessingScheduleMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Collections;
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
  private RequisitionGroupProgramScheduleRepository requisitionGroupProgramScheduleRepository;

  /**
   * Finds all ProcessingPeriods matching all of provided parameters.
   */
  public Page<ProcessingPeriod> searchPeriods(ProcessingPeriodSearchParams params,
                                              Pageable pageable) {
    params.validate();

    Program program = getById(programRepository, params.getProgramId(),
        ProgramMessageKeys.ERROR_NOT_FOUND_WITH_ID);
    Facility facility = getById(facilityRepository, params.getFacilityId(),
        FacilityMessageKeys.ERROR_NOT_FOUND_WITH_ID);

    ProcessingSchedule schedule;
    LocalDate startDate = params.getStartDate();
    LocalDate endDate = params.getEndDate();

    if (null != program && null != facility) {
      List<RequisitionGroupProgramSchedule> schedules = requisitionGroupProgramScheduleRepository
          .searchRequisitionGroupProgramSchedules(program, facility);
      if (schedules.isEmpty()) {
        LOGGER.warn("Cannot find Requisition Group Program Schedule for program {} and facility {}",
            program.getId(), facility.getId());
        return Pagination.getPage(Collections.emptyList(), pageable, 0);
      } else {
        schedule = schedules.get(0).getProcessingSchedule();
      }
    } else {
      schedule = getById(processingScheduleRepository, params.getProcessingScheduleId(),
          ProcessingScheduleMessageKeys.ERROR_NOT_FOUND_WITH_ID);
    }

    return periodRepository.search(schedule, startDate, endDate, pageable);
  }

  /**
   * Gets all the ProcessingPeriods based on program.
   */
  public Page<ProcessingPeriod> getProcessingPeriodsByProgramId(UUID programId,
                                              Pageable pageable) {

    Program program = getById(programRepository, programId,
            ProgramMessageKeys.ERROR_NOT_FOUND_WITH_ID);

    ProcessingSchedule schedule;

    List<RequisitionGroupProgramSchedule> schedules = requisitionGroupProgramScheduleRepository
            .searchRequisitionGroupProgramSchedules(program, null);
    if (schedules.isEmpty()) {
      LOGGER.warn("Cannot find Requisition Group Program Schedule for program {}",
              program.getId());
      return Pagination.getPage(Collections.emptyList(), pageable, 0);
    } else {
      schedule = schedules.get(0).getProcessingSchedule();
    }

    return periodRepository.search(schedule, null, null, pageable);
  }


  private <T> T getById(CrudRepository<T, UUID> repository, UUID id, String errorKey) {
    T object = null == id ? null : repository.findOne(id);
    if (null != id && null == object) {
      throw new NotFoundException(new Message(errorKey, id));
    }
    return object;
  }
}
