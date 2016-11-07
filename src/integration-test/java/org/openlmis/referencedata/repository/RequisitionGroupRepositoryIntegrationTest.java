package org.openlmis.referencedata.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Allow testing requisitionGroupRepository.
 */
public class RequisitionGroupRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<RequisitionGroup> {

  @Autowired
  private RequisitionGroupRepository repository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private ProcessingScheduleRepository processingScheduleRepository;

  private SupervisoryNode supervisoryNode;

  RequisitionGroupRepository getRepository() {
    return repository;
  }

  RequisitionGroup generateInstance() {
    int instanceNumber = getNextInstanceNumber();
    RequisitionGroup requisitionGroup = new RequisitionGroup();
    requisitionGroup.setCode("Code # " + instanceNumber);
    requisitionGroup.setName("ReqGr Name # " + instanceNumber);
    requisitionGroup.setSupervisoryNode(supervisoryNode);
    return requisitionGroup;
  }

  @Before
  public void setUp() {
    final String code = "code";

    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode(code);
    geographicLevel.setLevelNumber(1);
    geographicLevelRepository.save(geographicLevel);

    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode(code);
    geographicZone.setLevel(geographicLevel);
    geographicZoneRepository.save(geographicZone);

    FacilityType facilityType = new FacilityType();
    facilityType.setCode(code);
    facilityTypeRepository.save(facilityType);

    Facility facility = new Facility(code);
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setActive(true);
    facility.setEnabled(true);
    facilityRepository.save(facility);

    supervisoryNode = new SupervisoryNode();
    supervisoryNode.setCode(code);
    supervisoryNode.setFacility(facility);
    supervisoryNodeRepository.save(supervisoryNode);
  }

  @Test
  public void shouldAddScheduleToExistingRequisitionGroup() {
    RequisitionGroup actual = prepareAndSaveRequisitionGroupAndSchedule();

    assertEquals(1, actual.getRequisitionGroupProgramSchedules().size());

    RequisitionGroupProgramSchedule actualSchedule = actual
        .getRequisitionGroupProgramSchedules().get(0);
    assertNotNull(actualSchedule.getId());
    assertEquals("SCH-1", actualSchedule.getProcessingSchedule().getCode());
    assertEquals("PRO-1", actualSchedule.getProgram().getCode().toString());
    assertTrue(actualSchedule.isDirectDelivery());
  }

  @Test
  public void shouldRemoveScheduleFromRequisitionGroup() {
    RequisitionGroup group = prepareAndSaveRequisitionGroupAndSchedule();
    group.setRequisitionGroupProgramSchedules(new ArrayList<>());

    group = repository.save(group);

    assertEquals(0, group.getRequisitionGroupProgramSchedules().size());
  }

  private RequisitionGroup prepareAndSaveRequisitionGroupAndSchedule() {
    repository.save(generateInstance());
    RequisitionGroup existingGroup = repository.findAll().iterator().next();

    Program program = new Program("PRO-1");
    program = programRepository.save(program);
    ProcessingSchedule schedule = new ProcessingSchedule("SCH-1", "Monthly Schedule");
    schedule = processingScheduleRepository.save(schedule);

    RequisitionGroupProgramSchedule rgps =
        RequisitionGroupProgramSchedule.newRequisitionGroupProgramSchedule(existingGroup,
            program, schedule, true);

    List<RequisitionGroupProgramSchedule> schedules = new ArrayList<>();
    schedules.add(rgps);

    existingGroup.setRequisitionGroupProgramSchedules(schedules);

    repository.save(existingGroup);

    return repository.findOne(existingGroup.getId());
  }
}
