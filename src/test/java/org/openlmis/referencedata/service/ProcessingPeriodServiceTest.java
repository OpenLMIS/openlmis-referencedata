package org.openlmis.referencedata.service;

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
import org.openlmis.referencedata.exception.RequisitionGroupProgramScheduleException;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
  public void shouldFindPeriodsByProgramAndFacility()
        throws RequisitionGroupProgramScheduleException {
    doReturn(requisitionGroupProgramSchedule).when(repository)
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
