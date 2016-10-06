package org.openlmis.referencedata.repository;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.exception.RequisitionGroupProgramScheduleException;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

/**
 * Allow testing requisitionGroupProgramScheduleRepository.
 */
public class RequisitionGroupProgramScheduleRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<RequisitionGroupProgramSchedule> {

  @Autowired
  RequisitionGroupProgramScheduleRepository repository;

  @Autowired
  ProgramRepository programRepository;

  @Autowired
  ProcessingScheduleRepository scheduleRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  private Program program;
  private ProcessingSchedule schedule;
  private Facility facility;

  RequisitionGroupProgramScheduleRepository getRepository() {
    return this.repository;
  }

  RequisitionGroupProgramSchedule generateInstance() {
    RequisitionGroupProgramSchedule requisitionGroupProgramSchedule =
        new RequisitionGroupProgramSchedule();
    requisitionGroupProgramSchedule.setProgram(program);
    requisitionGroupProgramSchedule.setProcessingSchedule(schedule);
    requisitionGroupProgramSchedule.setDropOffFacility(facility);
    requisitionGroupProgramSchedule.setDirectDelivery(false);
    return requisitionGroupProgramSchedule;
  }

  @Before
  public void setUp() {
    final String code = "RequisitionGroup";

    program = new Program(code);
    programRepository.save(program);

    schedule = new ProcessingSchedule();
    schedule.setCode(code);
    schedule.setName(code);
    scheduleRepository.save(schedule);

    FacilityType facilityType = new FacilityType();
    facilityType.setCode("FT");
    facilityTypeRepository.save(facilityType);

    GeographicLevel level = new GeographicLevel();
    level.setCode("GL");
    level.setLevelNumber(1);
    geographicLevelRepository.save(level);

    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setLevel(level);
    geographicZone.setCode("GZ");
    geographicZoneRepository.save(geographicZone);

    facility = new Facility("F");
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setActive(true);
    facility.setEnabled(true);
    facilityRepository.save(facility);
  }

  @Test(expected = RequisitionGroupProgramScheduleException.class)
  public void shouldThrowExceptionWhenFindMoreThenOneRequisitionGroupProgramSchedule()
        throws RequisitionGroupProgramScheduleException {
    repository.save(generateInstance());
    repository.save(generateInstance());

    repository.searchRequisitionGroupProgramSchedule(program, facility);
  }

  @Test
  public void shouldReturnNullIfRequisitionGroupProgramScheduleIsNotFound()
        throws RequisitionGroupProgramScheduleException {
    RequisitionGroupProgramSchedule requisitionGroupProgramSchedule =
          repository.searchRequisitionGroupProgramSchedule(program, facility);

    assertEquals(requisitionGroupProgramSchedule, null);
  }
}
