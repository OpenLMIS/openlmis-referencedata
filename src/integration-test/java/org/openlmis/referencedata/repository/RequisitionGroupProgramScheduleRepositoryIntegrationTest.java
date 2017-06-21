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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.List;

/**
 * Allow testing requisitionGroupProgramScheduleRepository.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RequisitionGroupProgramScheduleRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<RequisitionGroupProgramSchedule> {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Autowired
  private RequisitionGroupProgramScheduleRepository repository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private ProcessingScheduleRepository scheduleRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private RequisitionGroupRepository requisitionGroupRepository;

  @Autowired
  private EntityManager entityManager;

  private RequisitionGroup requisitionGroup;
  private Program program;
  private ProcessingSchedule schedule;
  private Facility facility;

  RequisitionGroupProgramScheduleRepository getRepository() {
    return this.repository;
  }

  RequisitionGroupProgramSchedule generateInstance() {
    RequisitionGroupProgramSchedule requisitionGroupProgramSchedule = 
        RequisitionGroupProgramSchedule.newRequisitionGroupProgramSchedule(
            requisitionGroup, program, schedule, false);
    requisitionGroupProgramSchedule.setDropOffFacility(facility);
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
    
    SupervisoryNode supervisoryNode = SupervisoryNode.newSupervisoryNode(code, facility);
    supervisoryNodeRepository.save(supervisoryNode);

    requisitionGroup = new RequisitionGroup(code, code, supervisoryNode);
    requisitionGroupRepository.save(requisitionGroup);
  }

  @After
  public void tearDown() {
    repository.deleteAll();
  }

  @Test
  @Transactional(propagation = Propagation.NEVER)
  public void shouldThrowExceptionWhenSavingTheSameRequisitionGroupProgramSchedule() {
    expectedException.expectCause(isA(PersistenceException.class));

    repository.save(generateInstance());
    repository.save(generateInstance());
    entityManager.flush();
  }

  @Test
  public void shouldReturnNullIfRequisitionGroupProgramScheduleIsNotFound() {
    List<RequisitionGroupProgramSchedule> requisitionGroupProgramSchedule =
          repository.searchRequisitionGroupProgramSchedule(program, facility);

    assertEquals(requisitionGroupProgramSchedule.size(), 0);
  }

  @Test
  public void shouldReturnCorrectInstance() {
    RequisitionGroupProgramSchedule entity = generateInstance();
    repository.save(entity);

    requisitionGroup.setRequisitionGroupProgramSchedules(Lists.newArrayList(entity));
    requisitionGroup.setMemberFacilities(Sets.newHashSet(facility));
    requisitionGroupRepository.save(requisitionGroup);

    List<RequisitionGroupProgramSchedule> found = repository
        .searchRequisitionGroupProgramSchedule(program, facility);

    assertThat(found.get(0), is(entity));
  }
}
