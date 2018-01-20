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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.testbuilder.ProcessingPeriodDataBuilder;
import org.openlmis.referencedata.testbuilder.ProcessingScheduleDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ProcessingPeriodRepositoryIntegrationTest
      extends BaseCrudRepositoryIntegrationTest<ProcessingPeriod> {

  @Autowired
  private ProcessingPeriodRepository periodRepository;

  @Autowired
  private ProcessingScheduleRepository scheduleRepository;

  private ProcessingSchedule schedule;
  private PageRequest pageable = new PageRequest(0, 10);

  ProcessingPeriodRepository getRepository() {
    return this.periodRepository;
  }

  @Before
  public void setUp() {
    schedule = new ProcessingScheduleDataBuilder().buildWithoutId();
    scheduleRepository.save(schedule);
  }

  @Test
  public void shouldFindPeriodsByScheduleAndStartDate() {
    ProcessingPeriod period = periodRepository.save(generateInstance());

    ProcessingSchedule schedule2 = new ProcessingScheduleDataBuilder().buildWithoutId();
    scheduleRepository.save(schedule2);
    ProcessingPeriod period2 = generateInstance(schedule2, period.getStartDate().plusDays(1));
    period2 = periodRepository.save(period2);

    ProcessingPeriod period3 = generateInstance(period.getStartDate().plusDays(2));
    periodRepository.save(period3);

    Page<ProcessingPeriod> periods = periodRepository
        .findByProcessingScheduleAndStartDateLessThanEqual(
            schedule, period2.getStartDate(), pageable);
    assertEquals(1, periods.getTotalElements());
    assertEquals(period, periods.getContent().get(0));
  }

  @Test
  public void shouldFindPeriodsBySchedulePage() {
    periodRepository.save(generateInstance());
    periodRepository.save(generateInstance());

    ProcessingSchedule schedule2 = new ProcessingScheduleDataBuilder().buildWithoutId();
    scheduleRepository.save(schedule2);
    periodRepository.save(generateInstance(schedule2));

    Page<ProcessingPeriod> periods = periodRepository.findByProcessingSchedule(schedule, pageable);
    assertEquals(2, periods.getTotalElements());
    assertEquals(schedule, periods.getContent().get(0).getProcessingSchedule());
    assertEquals(schedule, periods.getContent().get(1).getProcessingSchedule());

    periods = periodRepository.findByProcessingSchedule(schedule2, pageable);
    assertEquals(1, periods.getTotalElements());
    assertEquals(schedule2, periods.getContent().get(0).getProcessingSchedule());
  }

  @Test
  public void shouldFindPeriodsByScheduleList() {
    periodRepository.save(generateInstance());
    periodRepository.save(generateInstance());

    ProcessingSchedule schedule2 = new ProcessingScheduleDataBuilder().buildWithoutId();
    scheduleRepository.save(schedule2);
    periodRepository.save(generateInstance(schedule2));

    List<ProcessingPeriod> periods = periodRepository.findByProcessingSchedule(schedule);
    assertEquals(2, periods.size());
    assertEquals(schedule, periods.get(0).getProcessingSchedule());
    assertEquals(schedule, periods.get(1).getProcessingSchedule());

    periods = periodRepository.findByProcessingSchedule(schedule2);
    assertEquals(1, periods.size());
    assertEquals(schedule2, periods.get(0).getProcessingSchedule());
  }

  @Test
  public void shouldFindPeriodsByStartDate() {
    ProcessingPeriod period = periodRepository.save(generateInstance());

    ProcessingPeriod period2 = generateInstance(period.getStartDate().plusDays(1));
    period2 = periodRepository.save(period2);

    ProcessingPeriod period3 = generateInstance(period.getStartDate().plusDays(2));
    periodRepository.save(period3);

    Page<ProcessingPeriod> periods = periodRepository
        .findByStartDateLessThanEqual(period2.getStartDate(), pageable);
    assertEquals(2, periods.getTotalElements());
    assertTrue(periods.getContent().get(0).getStartDate().isBefore(period3.getStartDate()));
    assertTrue(periods.getContent().get(1).getStartDate().isBefore(period3.getStartDate()));
  }

  @Test
  public void shouldFindPeriodsByNameAndSchedule() {
    ProcessingPeriod period = periodRepository.save(generateInstance());

    ProcessingPeriod period2 = generateInstance(period.getStartDate().plusDays(1));
    periodRepository.save(period2);

    ProcessingPeriod period3 = new ProcessingPeriodDataBuilder()
        .withName("some-other-name")
        .withSchedule(schedule)
        .buildAsNew();
    periodRepository.save(period3);

    Optional<ProcessingPeriod> result = periodRepository
        .findOneByNameAndProcessingSchedule(period.getName(), schedule);
    assertTrue(result.isPresent());
    assertEquals(period, result.get());
  }

  ProcessingPeriod generateInstance() {
    return new ProcessingPeriodDataBuilder()
        .withSchedule(schedule)
        .buildAsNew();
  }

  private ProcessingPeriod generateInstance(ProcessingSchedule processingSchedule) {
    return new ProcessingPeriodDataBuilder()
        .withSchedule(processingSchedule)
        .buildAsNew();
  }

  private ProcessingPeriod generateInstance(LocalDate startDate) {
    return new ProcessingPeriodDataBuilder()
        .withSchedule(schedule)
        .withStartDate(startDate)
        .buildAsNew();
  }

  private ProcessingPeriod generateInstance(ProcessingSchedule processingSchedule,
                                            LocalDate startDate) {
    return new ProcessingPeriodDataBuilder()
        .withSchedule(processingSchedule)
        .withStartDate(startDate)
        .buildAsNew();
  }
}