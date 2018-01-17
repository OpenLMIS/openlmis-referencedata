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

package org.openlmis.referencedata.testbuilder;

import org.apache.commons.lang.RandomStringUtils;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;

import java.time.LocalDate;
import java.util.UUID;

public class ProcessingPeriodDataBuilder {
  private UUID id = UUID.randomUUID();
  private ProcessingSchedule processingSchedule = new ProcessingScheduleDataBuilder().build();
  private String name = RandomStringUtils.randomAlphanumeric(5);
  private String description = RandomStringUtils.randomAlphanumeric(25);
  private LocalDate startDate = LocalDate.now();
  private LocalDate endDate = LocalDate.now().plusMonths(1);

  public ProcessingPeriodDataBuilder withSchedule(ProcessingSchedule schedule) {
    this.processingSchedule = schedule;
    return this;
  }

  /**
   * Set date duration for the given processing period.
   */
  public ProcessingPeriodDataBuilder withPeriod(LocalDate startDate, LocalDate endDate) {
    this.startDate = startDate;
    this.endDate = endDate;
    return this;
  }

  /**
   * Creates new instance of {@link ProcessingPeriod} without id.
   */
  public ProcessingPeriod buildAsNew() {
    ProcessingPeriod period = new ProcessingPeriod();
    period.setProcessingSchedule(processingSchedule);
    period.setName(name);
    period.setDescription(description);
    period.setStartDate(startDate);
    period.setEndDate(endDate);

    return period;
  }

  /**
   * Creates new instance of {@link ProcessingPeriod}.
   */
  public ProcessingPeriod build() {
    ProcessingPeriod period = buildAsNew();
    period.setId(id);

    return period;
  }
}
