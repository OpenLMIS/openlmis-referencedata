package org.openlmis.referencedata.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.exception.RequisitionGroupProgramScheduleException;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;


public class ProcessingPeriodServiceTest {

  @InjectMocks
  private ProcessingPeriodService periodService;

  @Mock
  private ProcessingPeriodRepository periodRepository;

  @Mock
  private RequisitionGroupProgramScheduleRepository repository;

  private RequisitionGroupProgramSchedule requisitionGroupProgramSchedule;
  private ProcessingPeriod period;
  private ProcessingSchedule schedule;
  private Program program;
  private Facility facility;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    requisitionGroupProgramSchedule = generateRequisitionGroupProgramSchedule();
    period = generatePeriod();
    schedule = generateSchedule();
    facility = generateFacility();
    program = generateProgram();
  }

  @Test
  public void shouldFindPeriodsByProgramAndFacility()
        throws RequisitionGroupProgramScheduleException {

    List<RequisitionGroupProgramSchedule> requisitionGroupProgramSchedules = new ArrayList<>();
    requisitionGroupProgramSchedules.add(requisitionGroupProgramSchedule);

    List<ProcessingPeriod> periods = new ArrayList<>();
    periods.add(period);

    when(repository.searchRequisitionGroupProgramSchedule(program, facility))
          .thenReturn(requisitionGroupProgramSchedules);
    when(periodRepository.searchPeriods(schedule, null))
          .thenReturn(periods);

    periodService.filterPeriods(program, facility);

    verify(repository).searchRequisitionGroupProgramSchedule(program, facility);

    verify(periodRepository).searchPeriods(schedule, null);
  }

  private RequisitionGroupProgramSchedule generateRequisitionGroupProgramSchedule() {
    requisitionGroupProgramSchedule = new RequisitionGroupProgramSchedule();
    requisitionGroupProgramSchedule.setProgram(program);
    requisitionGroupProgramSchedule.setDropOffFacility(facility);
    requisitionGroupProgramSchedule.setProcessingSchedule(schedule);
    return requisitionGroupProgramSchedule;
  }

  private ProcessingPeriod generatePeriod() {
    ProcessingPeriod period = new ProcessingPeriod();
    period.setProcessingSchedule(mock(ProcessingSchedule.class));
    period.setStartDate(LocalDate.now());
    period.setEndDate(LocalDate.now().plusDays(1));
    period.setName("name");
    return period;
  }

  private ProcessingSchedule generateSchedule() {
    ProcessingSchedule schedule = new ProcessingSchedule();
    schedule.setCode("code");
    schedule.setName("name");
    return schedule;
  }

  private Program generateProgram() {
    Program program = new Program("PROG");
    program.setName("name");
    return program;
  }

  private Facility generateFacility() {
    Facility facility = new Facility("F");
    facility.setType(mock(FacilityType.class));
    facility.setGeographicZone(mock(GeographicZone.class));
    facility.setName("facilityName");
    facility.setDescription("Test facility");
    facility.setActive(true);
    facility.setEnabled(true);
    return facility;
  }
}
