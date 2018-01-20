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

package org.openlmis.referencedata.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JaversSpringDataAuditable
public interface ProcessingPeriodRepository extends
    JpaRepository<ProcessingPeriod, UUID> {

  Page<ProcessingPeriod> findByProcessingScheduleAndStartDateLessThanEqual(
      ProcessingSchedule schedule, LocalDate startDate, Pageable pageable);

  Page<ProcessingPeriod> findByProcessingSchedule(ProcessingSchedule schedule, Pageable pageable);

  List<ProcessingPeriod> findByProcessingSchedule(ProcessingSchedule schedule);

  Page<ProcessingPeriod> findByStartDateLessThanEqual(LocalDate startDate, Pageable pageable);

  Optional<ProcessingPeriod> findOneByNameAndProcessingSchedule(String name,
                                                                ProcessingSchedule schedule);
}
