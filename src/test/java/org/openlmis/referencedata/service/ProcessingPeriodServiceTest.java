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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class ProcessingPeriodServiceTest {

  @InjectMocks
  private ProcessingPeriodService periodService;

  @Mock
  private ProcessingPeriodRepository periodRepository;

  @Mock
  private RequisitionGroupProgramScheduleRepository repository;

  @Mock
  private RequisitionGroupProgramSchedule requisitionGroupProgramSchedule;

  @Mock
  private ProcessingPeriod period;

  @Mock
  private ProcessingSchedule schedule;

  @Mock
  private Program program;

  @Mock
  private Facility facility;

  private List<ProcessingPeriod> periods;

  @Before
  public void setUp() {
    periods = new ArrayList<>();
    MockitoAnnotations.initMocks(this);
    when(requisitionGroupProgramSchedule.getProcessingSchedule()).thenReturn(schedule);
    generateInstances();
  }

  @Test
  public void shouldFindPeriodsByProgramAndFacility() {
    doReturn(Collections.singletonList(requisitionGroupProgramSchedule)).when(repository)
          .searchRequisitionGroupProgramSchedule(program, facility);
    doReturn(Arrays.asList(period)).when(periodRepository).searchPeriods(schedule, null);

    periodService.filterPeriods(program, facility);

    verify(repository).searchRequisitionGroupProgramSchedule(program, facility);
    verify(periodRepository).searchPeriods(schedule, null);
  }

  @Test
  public void shouldFindPeriodsWithinProvidedDateIfTheyExist() {
    List<ProcessingPeriod> matchedPeriods = new ArrayList<>();
    matchedPeriods.addAll(periods);
    matchedPeriods.remove(periods.get(0));
    when(periodRepository
        .searchPeriods(schedule, periods.get(0).getStartDate()))
        .thenReturn(matchedPeriods);

    List<ProcessingPeriod> receivedPeriods = periodService
        .searchPeriods(schedule, periods.get(0).getStartDate());

    assertEquals(4, receivedPeriods.size());
    for (ProcessingPeriod period : receivedPeriods) {
      assertEquals(schedule, period.getProcessingSchedule());
      assertTrue(periods.get(0).getStartDate().isAfter(period.getStartDate()));
    }
  }

  private void generateInstances() {
    final int periodCount = 5;
    for (int i = 0; i < periodCount; i++) {
      periods.add(generatePeriod(i));
    }
  }

  private ProcessingPeriod generatePeriod(int instanceNumber) {
    ProcessingPeriod period = new ProcessingPeriod();
    period.setId(UUID.randomUUID());
    period.setName("PeriodName" + instanceNumber);
    period.setDescription("PeriodDescription" + instanceNumber);
    period.setEndDate(LocalDate.now().plusDays(instanceNumber));
    period.setStartDate(LocalDate.now().minusDays(instanceNumber));
    period.setProcessingSchedule(schedule);
    return period;
  }

}
