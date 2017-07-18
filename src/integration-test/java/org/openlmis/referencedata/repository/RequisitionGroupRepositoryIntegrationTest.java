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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.javers.common.collections.Sets;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Allow testing requisitionGroupRepository.
 */
@SuppressWarnings({"PMD.TooManyMethods"})
public class RequisitionGroupRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<RequisitionGroup> {

  private static final String CODE = "code";

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
  private Facility facility;

  RequisitionGroupRepository getRepository() {
    return repository;
  }

  RequisitionGroup generateInstance() {
    int instanceNumber = getNextInstanceNumber();
    return generateInstance("Code # " + instanceNumber, "ReqGr Name # " + instanceNumber);
  }

  RequisitionGroup generateInstance(String code, String name) {
    RequisitionGroup requisitionGroup = new RequisitionGroup();
    requisitionGroup.setCode(code);
    requisitionGroup.setName(name);
    requisitionGroup.setSupervisoryNode(supervisoryNode);
    return requisitionGroup;
  }

  @Before
  public void setUp() {
    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode(CODE);
    geographicLevel.setLevelNumber(1);
    geographicLevelRepository.save(geographicLevel);

    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode(CODE);
    geographicZone.setLevel(geographicLevel);
    geographicZoneRepository.save(geographicZone);

    FacilityType facilityType = new FacilityType();
    facilityType.setCode(CODE);
    facilityTypeRepository.save(facilityType);

    facility = new Facility(CODE);
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setActive(true);
    facility.setEnabled(true);
    facilityRepository.save(facility);

    supervisoryNode = new SupervisoryNode();
    supervisoryNode.setCode(CODE);
    supervisoryNode.setFacility(facility);
    supervisoryNodeRepository.save(supervisoryNode);

    ProcessingSchedule schedule = new ProcessingSchedule("SCH-1", "Monthly Schedule");
    processingScheduleRepository.save(schedule);
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

  @Test
  public void shouldAddMemberFacilitiesToRequisitionGroup() {
    RequisitionGroup group = prepareAndSaveRequisitionGroupAndSchedule();
    group.setMemberFacilities(Sets.asSet(facility));
    RequisitionGroup actual = repository.save(group);

    assertNotNull(actual);
    assertEquals(1, actual.getMemberFacilities().size());
    assertEquals(CODE, actual.getMemberFacilities().iterator().next().getCode());
  }

  @Test
  public void shouldRemoveMemberFacilitiesFromRequisitionGroup() {
    RequisitionGroup group = prepareAndSaveRequisitionGroupAndSchedule();
    group.setMemberFacilities(Sets.asSet(facility));
    RequisitionGroup actual = repository.save(group);

    assertEquals(1, actual.getMemberFacilities().size());

    actual.setMemberFacilities(Collections.emptySet());
    actual = repository.save(actual);

    assertEquals(0, actual.getMemberFacilities().size());
  }

  @Test
  public void shouldFindGroupsWithSimilarCode() {
    RequisitionGroup requisitionGroup = generateInstance();
    repository.save(requisitionGroup);
    RequisitionGroup requisitionGroup1 = generateInstance();
    repository.save(requisitionGroup1);

    Pageable pageable = mockPageable(0, 10);

    searchGroupAndCheckResults(requisitionGroup.getCode(), null, null, null,
        pageable, 1, requisitionGroup);
  }

  @Test
  public void shouldFindGroupsWithSimilarCodeIgnoringCase() {
    RequisitionGroup requisitionGroup = generateInstance();
    repository.save(requisitionGroup);

    Pageable pageable = mockPageable(0, 10);

    searchGroupAndCheckResults(requisitionGroup.getCode().toUpperCase(), null, null, null,
        pageable, 1, requisitionGroup);
    searchGroupAndCheckResults(requisitionGroup.getCode().toLowerCase(), null, null, null,
        pageable, 1, requisitionGroup);
    searchGroupAndCheckResults("c", null, null, null, pageable, 1, requisitionGroup);
    searchGroupAndCheckResults("C", null, null, null, pageable, 1, requisitionGroup);
  }

  @Test
  public void shouldFindRequisitionGroupsWithSimilarName() {
    RequisitionGroup requisitionGroup = generateInstance();
    repository.save(requisitionGroup);

    Pageable pageable = mockPageable(0, 10);

    searchGroupAndCheckResults(null, "Req", null, null, pageable, 1, requisitionGroup);
  }

  @Test
  public void shouldFindRequisitionGroupsWithSimilarNameIgnoringCase() {
    RequisitionGroup requisitionGroup = generateInstance();
    repository.save(requisitionGroup);

    Pageable pageable = mockPageable(0, 10);

    searchGroupAndCheckResults(null, "req", null, null, pageable, 1, requisitionGroup);
    searchGroupAndCheckResults(null, "REQ", null, null, pageable, 1, requisitionGroup);
    searchGroupAndCheckResults(null, "ReQ", null, null, pageable, 1, requisitionGroup);
    searchGroupAndCheckResults(null, "rEq", null, null, pageable, 1, requisitionGroup);
  }

  @Test
  public void shouldFindRequisitionGroupsWithSimilarCodeOrName() {
    RequisitionGroup requisitionGroup = generateInstance();
    repository.save(requisitionGroup);
    RequisitionGroup requisitionGroup1 = generateInstance();
    repository.save(requisitionGroup1);

    Pageable pageable = mockPageable(0, 10);

    searchGroupAndCheckResults("Code", "Req", null, null,
        pageable, 2, requisitionGroup);
  }

  @Test
  public void shouldFindRequisitionGroupsWithSimilarCodeOrNameIgnoringCase() {
    RequisitionGroup requisitionGroup = generateInstance();
    repository.save(requisitionGroup);
    RequisitionGroup requisitionGroup1 = generateInstance();
    repository.save(requisitionGroup1);

    Pageable pageable = mockPageable(0, 10);

    searchGroupAndCheckResults("code", "req", null, null,
        pageable, 2, requisitionGroup);
    searchGroupAndCheckResults("CODE", "REQ", null, null,
        pageable, 2, requisitionGroup);
    searchGroupAndCheckResults("c", "Req", null, null, pageable, 2, requisitionGroup);
    searchGroupAndCheckResults("C", "ReQ", null, null, pageable, 2, requisitionGroup);
  }

  @Test
  public void shouldNotFindAnyFacilityForIncorrectCodeAndName() {
    Pageable pageable = mockPageable(0, 10);
    Page<RequisitionGroup> foundGroups = repository.search("Cucumber", "Tomato",
        null, null, pageable);

    assertEquals(0, foundGroups.getContent().size());
  }

  @Test
  public void shouldReturnRequisitionGroupsWithFullCount() {
    assertEquals(0, repository.count());

    for (int i = 0; i < 10; i++) {
      RequisitionGroup group  = generateInstance();
      repository.save(group);
    }

    // different code
    RequisitionGroup group = generateInstance();
    group.setCode("XXX");
    repository.save(group);

    assertEquals(11, repository.count());

    Pageable pageable = mockPageable(3, 0);

    Page<RequisitionGroup> result = repository.search("XXX", null, null, null, pageable);

    assertEquals(1, result.getContent().size());
    assertEquals(1, result.getTotalElements());

    result = repository.search("Code", null, null, null, pageable);

    assertEquals(3, result.getContent().size());
    assertEquals(10, result.getTotalElements());
  }

  @Test
  public void shouldReturnRequisitionGroupsWithWhenSearchingByProgramFullCount() {
    assertEquals(0, repository.count());

    for (int i = 0; i < 5; i++) {
      prepareAndSaveRequisitionGroupAndSchedule("C" + i, "PRO1");
    }
    for (int i = 5; i < 9; i++) {
      prepareAndSaveRequisitionGroupAndSchedule("C" + i, "PRO2");

    }

    assertEquals(9, repository.count());

    Pageable pageable = mockPageable(2, 0);
    Program programOne = programRepository.findByCode(Code.code("PRO1"));
    Program programTwo = programRepository.findByCode(Code.code("PRO2"));

    Page<RequisitionGroup> result = repository.search(null, null, programOne, null, pageable);

    assertEquals(2, result.getContent().size());
    assertEquals(5, result.getTotalElements());

    result = repository.search(null, null, programTwo, null, pageable);

    assertEquals(2, result.getContent().size());
    assertEquals(4, result.getTotalElements());
  }

  @Test
  public void shouldFindRequisitionGroupsBySupervisoryNodes() {
    RequisitionGroup requisitionGroup = generateInstance();
    repository.save(requisitionGroup);

    SupervisoryNode supervisoryNode1 = new SupervisoryNode();
    supervisoryNode1.setCode("some-code");
    supervisoryNode1.setFacility(facility);
    supervisoryNodeRepository.save(supervisoryNode1);
    RequisitionGroup requisitionGroup1 = generateInstance();
    requisitionGroup1.setSupervisoryNode(supervisoryNode1);
    repository.save(requisitionGroup1);

    SupervisoryNode supervisoryNode2 = new SupervisoryNode();
    supervisoryNode2.setCode("other-code");
    supervisoryNode2.setFacility(facility);
    supervisoryNodeRepository.save(supervisoryNode2);
    RequisitionGroup requisitionGroup2 = generateInstance();
    requisitionGroup2.setSupervisoryNode(supervisoryNode2);
    repository.save(requisitionGroup2);

    Pageable pageable = mockPageable(0, 10);

    List<SupervisoryNode> nodes = new ArrayList<>();
    nodes.add(supervisoryNode);
    nodes.add(supervisoryNode1);

    searchGroupAndCheckResults(null, null, null, nodes,
        pageable, 2, requisitionGroup);

    searchGroupAndCheckResults(null, null, null, Arrays.asList(supervisoryNode),
        pageable, 1, requisitionGroup);
  }

  @Test
  public void shouldFindRequisitionGroupsByProgram() {

    RequisitionGroup requisitionGroup = prepareAndSaveRequisitionGroupAndSchedule();
    requisitionGroup = repository.save(requisitionGroup);

    Pageable pageable = mockPageable(0, 10);

    searchGroupAndCheckResults(null, null,
        requisitionGroup.getRequisitionGroupProgramSchedules().get(0).getProgram(),
        null, pageable, 1, requisitionGroup);
  }

  @Test
  public void shouldSortByName() {
    RequisitionGroup requisitionGroup = generateInstance();
    requisitionGroup.setName("RG-a");
    repository.save(requisitionGroup);

    RequisitionGroup requisitionGroup1 = generateInstance();
    requisitionGroup1.setName("RG-b");
    repository.save(requisitionGroup1);

    Pageable pageable = mockPageable(0, 10);

    Page<RequisitionGroup> foundPage = repository.search(null, "RG", null,
        null, pageable);
    assertEquals(2, foundPage.getContent().size());
    assertEquals(requisitionGroup.getName(), foundPage.getContent().get(0).getName());
    assertEquals(requisitionGroup1.getName(), foundPage.getContent().get(1).getName());
  }

  @Test
  public void shouldReturnEmptyListIfSearchParametersAreNotProvided() {
    Pageable pageable = mockPageable(0, 10);

    Page<RequisitionGroup> foundPage = repository.search(null, null, null,
        null, pageable);
    assertEquals(0, foundPage.getContent().size());

    foundPage = repository.search(null, null, null,
        Collections.emptyList(), pageable);
    assertEquals(0, foundPage.getContent().size());
  }

  private void searchGroupAndCheckResults(String code, String name, Program program,
                                          List<SupervisoryNode> supervisoryNodes,
                                          Pageable pageable, int expectedSize,
                                          RequisitionGroup requisitionGroup) {
    Page<RequisitionGroup> foundPage = repository.search(code, name, program,
        supervisoryNodes, pageable);

    assertEquals(expectedSize, foundPage.getContent().size());

    assertEquals(requisitionGroup.getName(), foundPage.getContent().get(0).getName());
  }

  private Pageable mockPageable(int pageSize, int pageNumber) {
    Pageable pageable = mock(Pageable.class);
    given(pageable.getPageNumber()).willReturn(pageNumber);
    given(pageable.getPageSize()).willReturn(pageSize);
    return pageable;
  }

  private RequisitionGroup prepareAndSaveRequisitionGroupAndSchedule() {
    return prepareAndSaveRequisitionGroupAndSchedule(null, "PRO-1");
  }

  private RequisitionGroup prepareAndSaveRequisitionGroupAndSchedule(String code,
                                                                     String programCode) {
    RequisitionGroup group;
    if (code != null) {
      group = repository.save(generateInstance(code, code));
    } else {
      group = repository.save(generateInstance());
    }

    Program program = programRepository.findByCode(Code.code(programCode));
    if (program == null) {
      program = new Program(programCode);
      program = programRepository.save(program);
    }

    ProcessingSchedule schedule = processingScheduleRepository.findAll().iterator().next();

    RequisitionGroupProgramSchedule rgps =
        RequisitionGroupProgramSchedule.newRequisitionGroupProgramSchedule(group,
            program, schedule, true);

    List<RequisitionGroupProgramSchedule> schedules = new ArrayList<>();
    schedules.add(rgps);

    group.setRequisitionGroupProgramSchedules(schedules);

    return repository.save(group);
  }
}
