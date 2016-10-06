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

import java.util.Arrays;

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

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(requisitionGroupProgramSchedule.getProcessingSchedule()).thenReturn(schedule);
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
}
