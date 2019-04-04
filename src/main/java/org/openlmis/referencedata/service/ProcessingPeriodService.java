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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
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
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

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
    Profiler profiler = new Profiler("SEARCH_PERIODS_BY_PARAMS");
    profiler.setLogger(LOGGER);

    params.validate();

    profiler.start("CHECK_IF_PROGRAM_EXISTS");
    Boolean programExists = existsById(programRepository, params.getProgramId(),
        ProgramMessageKeys.ERROR_NOT_FOUND_WITH_ID);

    profiler.start("CHECK_IF_FACILITY_EXISTS");
    Boolean facilityExists = existsById(facilityRepository, params.getFacilityId(),
        FacilityMessageKeys.ERROR_NOT_FOUND_WITH_ID);

    ProcessingSchedule schedule;
    LocalDate startDate = params.getStartDate();
    LocalDate endDate = params.getEndDate();
    Collection<UUID> ids = params.getIds();

    if (programExists) {
      profiler.start("SEARCH_FOR_REQUISITION_GROUP_PROGRAM_SCHEDULE");
      List<RequisitionGroupProgramSchedule> schedules = requisitionGroupProgramScheduleRepository
          .searchRequisitionGroupProgramSchedules(params.getProgramId(), params.getFacilityId());
      if (schedules.isEmpty()) {
        if (!facilityExists) {
          LOGGER.warn("Cannot find Requisition Group Program Schedule for program {}",
                  params.getProgramId());
        } else {
          LOGGER.warn("Cannot find Requisition Group Program Schedule for"
                  + "program {} and facility {}", params.getProgramId(), params.getFacilityId());
        }
        return Pagination.getPage(Collections.emptyList(), pageable, 0);
      } else {
        schedule = schedules.get(0).getProcessingSchedule();
      }
    } else {
      profiler.start("CHECK_IF_SCHEDULE_EXISTS");
      Boolean scheduleExists = existsById(processingScheduleRepository,
          params.getProcessingScheduleId(), ProcessingScheduleMessageKeys.ERROR_NOT_FOUND_WITH_ID);

      profiler.start("FIND_SCHEDULE_IN_DB");
      schedule = scheduleExists
          ? processingScheduleRepository.findOne(params.getProcessingScheduleId()) : null;
    }

    profiler.start("SEARCH_FOR_PERIODS");
    Page<ProcessingPeriod> periods =
        periodRepository.search(schedule, startDate, endDate, ids, pageable);

    profiler.stop().log();
    return periods;
  }

  private <T> Boolean existsById(CrudRepository<T, UUID> repository, UUID id, String errorKey) {
    Boolean exists = id != null && repository.exists(id);
    if (null != id && !exists) {
      throw new NotFoundException(new Message(errorKey, id));
    }
    return exists;
  }
}
