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
    RequisitionGroup requisitionGroup = new RequisitionGroup();
    requisitionGroup.setCode("Code # " + instanceNumber);
    requisitionGroup.setName("ReqGr Name # " + instanceNumber);
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
  public void shouldFindFacilitiesWithSimilarName() {
    RequisitionGroup requisitionGroup = generateInstance();
    repository.save(requisitionGroup);

    Pageable pageable = mockPageable(0, 10);

    searchGroupAndCheckResults(null, "Req", null, null, pageable, 1, requisitionGroup);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarNameIgnoringCase() {
    RequisitionGroup requisitionGroup = generateInstance();
    repository.save(requisitionGroup);

    Pageable pageable = mockPageable(0, 10);

    searchGroupAndCheckResults(null, "req", null, null, pageable, 1, requisitionGroup);
    searchGroupAndCheckResults(null, "REQ", null, null, pageable, 1, requisitionGroup);
    searchGroupAndCheckResults(null, "ReQ", null, null, pageable, 1, requisitionGroup);
    searchGroupAndCheckResults(null, "rEq", null, null, pageable, 1, requisitionGroup);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCodeOrName() {
    RequisitionGroup requisitionGroup = generateInstance();
    repository.save(requisitionGroup);
    RequisitionGroup requisitionGroup1 = generateInstance();
    repository.save(requisitionGroup1);

    Pageable pageable = mockPageable(0, 10);

    searchGroupAndCheckResults("Code", "Req", null, null,
        pageable, 2, requisitionGroup);
  }

  @Test
  public void shouldFindFacilitiesWithSimilarCodeOrNameIgnoringCase() {
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

  /*@Test
  public void shouldFindFacilitiesBySupervisoryNodes() {
    RequisitionGroup requisitionGroup = generateInstance();
    repository.save(requisitionGroup);

    SupervisoryNode supervisoryNode1 = new SupervisoryNode();
    supervisoryNode1.setCode("some-code");
    supervisoryNode1.setFacility(facility);
    supervisoryNodeRepository.save(supervisoryNode1);
    RequisitionGroup requisitionGroup1 = generateInstance();
    requisitionGroup1.setSupervisoryNode(supervisoryNode1);
    repository.save(requisitionGroup1);

    Pageable pageable = mockPageable(0, 10);

    searchGroupAndCheckResults(null, null, null, Arrays.asList(supervisoryNode),
        pageable, 1, requisitionGroup);
  }*/

  /*@Test
  public void shouldFindFacilitiesByProgram() {

    RequisitionGroup requisitionGroup = prepareAndSaveRequisitionGroupAndSchedule();
    requisitionGroup = repository.save(requisitionGroup);

    Pageable pageable = mockPageable(0, 10);

    searchGroupAndCheckResults(null, null,
        requisitionGroup.getRequisitionGroupProgramSchedules().get(0).getProgram(),
        null, pageable, 1, requisitionGroup);
  }*/

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
