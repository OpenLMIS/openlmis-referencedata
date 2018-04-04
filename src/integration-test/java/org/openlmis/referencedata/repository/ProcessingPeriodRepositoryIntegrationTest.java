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

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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

@SuppressWarnings({"PMD.TooManyMethods"})
public class ProcessingPeriodRepositoryIntegrationTest
      extends BaseCrudRepositoryIntegrationTest<ProcessingPeriod> {

  @Autowired
  private ProcessingPeriodRepository periodRepository;

  @Autowired
  private ProcessingScheduleRepository scheduleRepository;

  private ProcessingSchedule schedule;
  private PageRequest pageable = new PageRequest(0, 10);

  private ProcessingPeriod period1;
  private ProcessingPeriod period2;
  private ProcessingPeriod period3;

  ProcessingPeriodRepository getRepository() {
    return this.periodRepository;
  }

  @Before
  public void setUp() {
    schedule = new ProcessingScheduleDataBuilder().buildWithoutId();
    scheduleRepository.save(schedule);

    period1 = periodRepository.save(generateInstance());
    period2 = periodRepository.save(
        generateInstance(period1.getStartDate().plusMonths(1), period1.getEndDate().plusMonths(1)));
    period3 = periodRepository.save(
        generateInstance(period2.getStartDate().plusMonths(1), period2.getEndDate().plusMonths(1)));
  }

  @Test
  public void shouldFindPeriodsStartDate() {
    Page<ProcessingPeriod> periods = periodRepository
        .search(schedule, period2.getEndDate(), null, pageable);

    assertEquals(2, periods.getTotalElements());
    assertThat(periods.getContent(), hasItems(period2, period3));
  }

  @Test
  public void shouldFindPeriodsByEndDate() {
    Page<ProcessingPeriod> periods = periodRepository
        .search(schedule, null, period2.getStartDate(), pageable);

    assertEquals(2, periods.getTotalElements());
    assertThat(periods.getContent(), hasItems(period1, period2));
  }

  @Test
  public void shouldFindPeriodsByStartDateAndEndDate() {
    Page<ProcessingPeriod> periods = periodRepository
        .search(schedule, period1.getEndDate(), period2.getStartDate(), pageable);

    assertEquals(2, periods.getTotalElements());
    assertThat(periods.getContent(), hasItems(period1, period2));
  }

  @Test
  public void shouldFindPeriodsBySchedulePage() {
    ProcessingSchedule schedule2 = new ProcessingScheduleDataBuilder().buildWithoutId();
    scheduleRepository.save(schedule2);
    periodRepository.save(generateInstance(schedule2));

    Page<ProcessingPeriod> periods = periodRepository.search(schedule, null, null, pageable);
    assertEquals(3, periods.getTotalElements());
    assertEquals(schedule, periods.getContent().get(0).getProcessingSchedule());
    assertEquals(schedule, periods.getContent().get(1).getProcessingSchedule());
    assertEquals(schedule, periods.getContent().get(2).getProcessingSchedule());

    periods = periodRepository.search(schedule2, null, null, pageable);
    assertEquals(1, periods.getTotalElements());
    assertEquals(schedule2, periods.getContent().get(0).getProcessingSchedule());
  }

  @Test
  public void shouldFindPeriodsByScheduleList() {
    ProcessingSchedule schedule2 = new ProcessingScheduleDataBuilder().buildWithoutId();
    scheduleRepository.save(schedule2);
    periodRepository.save(generateInstance(schedule2));

    List<ProcessingPeriod> periods = periodRepository.findByProcessingSchedule(schedule);
    assertEquals(3, periods.size());
    assertEquals(schedule, periods.get(0).getProcessingSchedule());
    assertEquals(schedule, periods.get(1).getProcessingSchedule());
    assertEquals(schedule, periods.get(2).getProcessingSchedule());

    periods = periodRepository.findByProcessingSchedule(schedule2);
    assertEquals(1, periods.size());
    assertEquals(schedule2, periods.get(0).getProcessingSchedule());
  }

  @Test
  public void shouldFindPeriodsByScheduleAndStartDateAndEndDate() {
    ProcessingSchedule newSchedule = scheduleRepository
        .save(new ProcessingScheduleDataBuilder().buildWithoutId());

    ProcessingPeriod period4 = periodRepository.save(generateInstance(newSchedule));
    ProcessingPeriod period5 = periodRepository.save(generateInstance(newSchedule,
        period1.getStartDate().plusMonths(1), period1.getEndDate().plusMonths(1)));
    periodRepository.save(generateInstance(newSchedule,
        period2.getStartDate().plusMonths(1), period2.getEndDate().plusMonths(1)));

    Page<ProcessingPeriod> periods = periodRepository
        .search(newSchedule, period4.getEndDate(), period5.getStartDate(), pageable);

    assertEquals(2, periods.getTotalElements());
    assertThat(periods.getContent(), hasItems(period4, period5));
  }

  @Test
  public void shouldFindPeriodsByNameAndSchedule() {
    ProcessingPeriod period = periodRepository.save(generateInstance());

    ProcessingPeriod period2 = generateInstance(period.getStartDate().plusMonths(1),
        period.getEndDate().plusMonths(1));
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

  private ProcessingPeriod generateInstance(LocalDate startDate, LocalDate endDate) {
    return new ProcessingPeriodDataBuilder()
        .withSchedule(schedule)
        .withStartDate(null != startDate ? startDate : LocalDate.now())
        .withEndDate(null != endDate ? endDate : LocalDate.now().plusMonths(1))
        .buildAsNew();
  }

  private ProcessingPeriod generateInstance(ProcessingSchedule processingSchedule,
      LocalDate startDate, LocalDate endDate) {
    return new ProcessingPeriodDataBuilder()
        .withSchedule(processingSchedule)
        .withStartDate(null != startDate ? startDate : LocalDate.now())
        .withEndDate(null != endDate ? endDate : LocalDate.now().plusMonths(1))
        .buildAsNew();
  }
}