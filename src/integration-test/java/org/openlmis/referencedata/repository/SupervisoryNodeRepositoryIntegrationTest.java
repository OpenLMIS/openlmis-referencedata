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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.javers.common.collections.Sets.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Sets;
import java.util.UUID;
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
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.ProcessingScheduleDataBuilder;
import org.openlmis.referencedata.testbuilder.RequisitionGroupDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.web.SupervisoryNodeSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;

@SuppressWarnings("PMD.TooManyMethods")
public class SupervisoryNodeRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<SupervisoryNode> {

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
  private ProcessingScheduleRepository scheduleRepository;

  @Autowired
  private RequisitionGroupRepository requisitionGroupRepository;

  @Autowired
  private RequisitionGroupProgramScheduleRepository requisitionGroupProgramScheduleRepository;

  private Facility facility;
  private FacilityType facilityType;
  private GeographicLevel geographicLevel;
  private GeographicZone geographicZone;

  @Override
  CrudRepository<SupervisoryNode, UUID> getRepository() {
    return supervisoryNodeRepository;
  }

  @Before
  public void setUp() {
    String code = "code";

    geographicLevel = new GeographicLevel();
    geographicLevel.setCode(code);
    geographicLevel.setLevelNumber(1);
    geographicLevelRepository.save(geographicLevel);

    geographicZone = new GeographicZone();
    geographicZone.setCode(code);
    geographicZone.setLevel(geographicLevel);
    geographicZoneRepository.save(geographicZone);

    facilityType = new FacilityType();
    facilityType.setCode(code);
    facilityTypeRepository.save(facilityType);

    facility = new Facility(code);
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setActive(true);
    facility.setEnabled(true);
    facilityRepository.save(facility);
  }

  @Override
  SupervisoryNode generateInstance() {
    return new SupervisoryNodeDataBuilder()
        .withoutId()
        .withFacility(facility)
        .build();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void duplicateCodeShouldThrowException() {
    // given a SN in the db
    SupervisoryNode sn1 = generateInstance();
    supervisoryNodeRepository.save(sn1);

    // when a new SN is made with the same code
    SupervisoryNode sn2 = generateInstance();
    sn2.setCode(sn1.getCode());
    supervisoryNodeRepository.save(sn2);

    // then a DB constraint is found
    supervisoryNodeRepository.flush();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void nameWithNullValueShouldThrowException() {
    // given a SN in the db
    SupervisoryNode sn = generateInstance();
    sn.setName(null);
    supervisoryNodeRepository.save(sn);

    // then a DB constraint is found
    supervisoryNodeRepository.flush();
  }

  @Test
  public void nullFacilityShouldNotThrowException() {
    // given a SN in the db
    SupervisoryNode sn = generateInstance();
    sn.setFacility(null);
    supervisoryNodeRepository.save(sn);

    // then a DB constraint is found
    supervisoryNodeRepository.flush();
  }

  @Test
  public void shouldFindSupervisoryNodesByCodeAndName() {
    supervisoryNodeRepository.save(new SupervisoryNodeDataBuilder()
        .withoutId()
        .withFacility(facility)
        .withName("some-name")
        .withCode("some-code")
        .build());

    supervisoryNodeRepository.save(new SupervisoryNodeDataBuilder()
        .withoutId()
        .withFacility(facility)
        .withName("some-other-name")
        .withCode("some-other-code")
        .build());

    supervisoryNodeRepository.save(new SupervisoryNodeDataBuilder()
        .withoutId()
        .withFacility(facility)
        .withName("node-name")
        .withCode("node-code")
        .build());

    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());

    SupervisoryNodeSearchParams params = new SupervisoryNodeSearchParams(
        "name", "code", null, null, null, null);
    assertEquals(3, supervisoryNodeRepository
        .search(params, null).getTotalElements());
    params.setCode("CODE");
    params.setName("NAME");
    assertEquals(3, supervisoryNodeRepository
        .search(params, null).getTotalElements());
    params.setCode("CoDe");
    params.setName("nAMe");
    assertEquals(3, supervisoryNodeRepository
        .search(params, null).getTotalElements());

    params.setName("some-name");
    params.setCode(null);
    assertEquals(1, supervisoryNodeRepository
        .search(params, null).getTotalElements());
    params.setCode("random-string");
    assertEquals(0, supervisoryNodeRepository
        .search(params, null).getTotalElements());
    params.setCode("some-code");
    params.setName(null);
    assertEquals(1, supervisoryNodeRepository
        .search(params, null).getTotalElements());
    params.setName("random-string");
    assertEquals(0, supervisoryNodeRepository
        .search(params, null).getTotalElements());
  }

  @Test
  public void shouldSearchByZoneId() {
    supervisoryNodeRepository.save(generateInstance());

    GeographicZone geographicZone = geographicZoneRepository.save(
        new GeographicZoneDataBuilder()
            .withLevel(geographicLevel)
            .buildAsNew());
    Facility facility = facilityRepository.save(
        new FacilityDataBuilder()
            .withType(facilityType)
            .withGeographicZone(geographicZone)
            .withoutOperator()
            .buildAsNew());
    SupervisoryNode node = supervisoryNodeRepository.save(
        new SupervisoryNodeDataBuilder().withFacility(facility).withoutId().build());

    SupervisoryNodeSearchParams params = new SupervisoryNodeSearchParams(null, null, null, null,
        geographicZone.getId(), null);
    Page<SupervisoryNode> result = supervisoryNodeRepository
        .search(params, null);

    assertEquals(1, result.getTotalElements());
    assertEquals(node, result.getContent().get(0));
  }

  @Test
  public void shouldSearchByIds() {
    SupervisoryNode node1 = supervisoryNodeRepository.save(generateInstance());
    SupervisoryNode node2 = supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());

    SupervisoryNodeSearchParams params = new SupervisoryNodeSearchParams(null, null, null, null,
        null, asSet(node1.getId(), node2.getId()));
    Page<SupervisoryNode> result = supervisoryNodeRepository
        .search(params, null);

    assertEquals(2, result.getTotalElements());
    assertThat(result.getContent(), hasItems(node1, node2));
  }

  @Test
  public void shouldPaginate() {
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());

    SupervisoryNodeSearchParams params = new SupervisoryNodeSearchParams();
    assertEquals(2, supervisoryNodeRepository
        .search(params, new PageRequest(0, 2)).getContent().size());
    assertEquals(2, supervisoryNodeRepository
        .search(params, new PageRequest(1, 2)).getContent().size());
    assertEquals(1, supervisoryNodeRepository
        .search(params, new PageRequest(2, 2)).getContent().size());

    assertEquals(4, supervisoryNodeRepository
        .search(params, new PageRequest(0, 4)).getContent().size());
    assertEquals(1, supervisoryNodeRepository
        .search(params, new PageRequest(1, 4)).getContent().size());
  }

  @Test
  public void shouldReturnAllElementsIfNoSearchParamsProvided() {
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());

    SupervisoryNodeSearchParams params = new SupervisoryNodeSearchParams();
    assertEquals(5, supervisoryNodeRepository
        .search(params, null).getTotalElements());
    assertEquals(5, supervisoryNodeRepository
        .search(params, null).getContent().size());
    assertEquals(5, supervisoryNodeRepository
        .search(params, null).getNumberOfElements());
    assertEquals(0, supervisoryNodeRepository
        .search(params, null).getNumber());
    assertEquals(0, supervisoryNodeRepository
        .search(params, null).getSize());
    assertNull(supervisoryNodeRepository
        .search(params, null).getSort());
  }

  @Test
  public void shouldSearchByProgramId() {
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());

    Program program = programRepository.save(new Program("program-code"));

    SupervisoryNodeSearchParams params = new SupervisoryNodeSearchParams(null,
        null, null, program.getId(), null, null);
    Page<SupervisoryNode> result = supervisoryNodeRepository
        .search(params, null);

    assertEquals(0, result.getTotalElements());

    ProcessingSchedule schedule = scheduleRepository
        .save(new ProcessingScheduleDataBuilder().buildWithoutId());
    SupervisoryNode supervisoryNode = supervisoryNodeRepository.save(generateInstance());

    RequisitionGroup requisitionGroup = requisitionGroupRepository
        .save(new RequisitionGroupDataBuilder()
            .withSupervisoryNode(supervisoryNode)
            .buildAsNew());

    requisitionGroupProgramScheduleRepository.save(RequisitionGroupProgramSchedule
        .newRequisitionGroupProgramSchedule(requisitionGroup, program, schedule, false));

    result = supervisoryNodeRepository
        .search(params, null);

    assertEquals(1, result.getTotalElements());
    assertEquals(supervisoryNode, result.getContent().get(0));
  }

  @Test
  public void shouldSearchByFacilityId() {
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());
    supervisoryNodeRepository.save(generateInstance());

    Facility newFacility = facilityRepository.save(
        new FacilityDataBuilder()
            .withType(facilityType)
            .withGeographicZone(geographicZone)
            .withoutOperator()
            .buildAsNew());

    SupervisoryNodeSearchParams params = new SupervisoryNodeSearchParams(null,
        null, newFacility.getId(), null, null, null);
    Page<SupervisoryNode> result = supervisoryNodeRepository
        .search(params, null);
    assertEquals(0, result.getTotalElements());

    SupervisoryNode supervisoryNode = supervisoryNodeRepository.save(generateInstance());

    requisitionGroupRepository.save(new RequisitionGroupDataBuilder()
        .withSupervisoryNode(supervisoryNode)
        .withMemberFacilities(asSet(newFacility))
        .buildAsNew());

    result = supervisoryNodeRepository
        .search(params, null);

    assertEquals(1, result.getTotalElements());
    assertEquals(supervisoryNode, result.getContent().get(0));
  }

  @Test
  public void shouldAssignChildNodes() {
    // given
    SupervisoryNode supervisoryNode1 = supervisoryNodeRepository.save(generateInstance());
    SupervisoryNode supervisoryNode2 = supervisoryNodeRepository.save(generateInstance());
    SupervisoryNode supervisoryNode3 = supervisoryNodeRepository.save(generateInstance());

    // when
    supervisoryNode1.assignChildNodes(Sets.newHashSet(supervisoryNode2, supervisoryNode3));

    supervisoryNodeRepository.saveAndFlush(supervisoryNode1);

    // then
    supervisoryNode1 = supervisoryNodeRepository.getOne(supervisoryNode1.getId());
    supervisoryNode2 = supervisoryNodeRepository.getOne(supervisoryNode2.getId());
    supervisoryNode3 = supervisoryNodeRepository.getOne(supervisoryNode3.getId());

    assertThat(supervisoryNode1, is(notNullValue()));
    assertThat(supervisoryNode2, is(notNullValue()));
    assertThat(supervisoryNode3, is(notNullValue()));

    assertThat(supervisoryNode1.getChildNodes(), hasSize(2));
    assertThat(supervisoryNode1.getChildNodes(), hasItems(supervisoryNode2, supervisoryNode3));

    assertThat(supervisoryNode2.getParentNode(), is(supervisoryNode1));
    assertThat(supervisoryNode3.getParentNode(), is(supervisoryNode1));
  }

  @Test
  public void shouldRemoveChildNodes() {
    // given
    SupervisoryNode supervisoryNode1 = supervisoryNodeRepository.save(generateInstance());
    SupervisoryNode supervisoryNode2 = supervisoryNodeRepository.save(generateInstance());
    SupervisoryNode supervisoryNode3 = supervisoryNodeRepository.save(generateInstance());

    supervisoryNode1.assignChildNodes(Sets.newHashSet(supervisoryNode2, supervisoryNode3));

    supervisoryNodeRepository.saveAndFlush(supervisoryNode1);

    // when
    supervisoryNode1.assignChildNodes(Sets.newHashSet());

    supervisoryNodeRepository.saveAndFlush(supervisoryNode1);

    // then
    supervisoryNode1 = supervisoryNodeRepository.getOne(supervisoryNode1.getId());
    supervisoryNode2 = supervisoryNodeRepository.getOne(supervisoryNode2.getId());
    supervisoryNode3 = supervisoryNodeRepository.getOne(supervisoryNode3.getId());

    assertThat(supervisoryNode1, is(notNullValue()));
    assertThat(supervisoryNode2, is(notNullValue()));
    assertThat(supervisoryNode3, is(notNullValue()));

    assertThat(supervisoryNode1.getChildNodes(), hasSize(0));

    assertThat(supervisoryNode2.getParentNode(), is(nullValue()));
    assertThat(supervisoryNode3.getParentNode(), is(nullValue()));
  }
}
