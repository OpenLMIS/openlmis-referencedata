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
import static org.javers.common.collections.Sets.asSet;
import static org.junit.Assert.assertEquals;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;

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

  private Facility facility;
  private FacilityType facilityType;
  private GeographicLevel geographicLevel;

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

    GeographicZone geographicZone = new GeographicZone();
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

    assertEquals(3, supervisoryNodeRepository
        .search("code", "name", null, null, null, null, null).getTotalElements());
    assertEquals(3, supervisoryNodeRepository
        .search("CODE", "NAME", null, null, null, null, null).getTotalElements());
    assertEquals(3, supervisoryNodeRepository
        .search("CoDe", "nAMe", null, null, null, null, null).getTotalElements());

    assertEquals(1, supervisoryNodeRepository
        .search(null, "some-name", null, null, null, null, null).getTotalElements());
    assertEquals(0, supervisoryNodeRepository
        .search("random-string", "some-name", null, null, null, null, null).getTotalElements());
    assertEquals(1, supervisoryNodeRepository
        .search("some-code", null, null, null, null, null, null).getTotalElements());
    assertEquals(0, supervisoryNodeRepository
        .search("some-code", "random-string", null, null, null, null, null).getTotalElements());
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

    Page<SupervisoryNode> result = supervisoryNodeRepository
        .search(null, null, geographicZone.getId(), null, null, null, null);

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

    Page<SupervisoryNode> result = supervisoryNodeRepository
        .search(null, null, null, null, null, asSet(node1.getId(), node2.getId()), null);

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

    assertEquals(2, supervisoryNodeRepository
        .search(null, null, null, null, null, null, new PageRequest(0, 2)).getContent().size());
    assertEquals(2, supervisoryNodeRepository
        .search(null, null, null, null, null, null, new PageRequest(1, 2)).getContent().size());
    assertEquals(1, supervisoryNodeRepository
        .search(null, null, null, null, null, null, new PageRequest(2, 2)).getContent().size());

    assertEquals(4, supervisoryNodeRepository
        .search(null, null, null, null, null, null, new PageRequest(0, 4)).getContent().size());
    assertEquals(1, supervisoryNodeRepository
        .search(null, null, null, null, null, null, new PageRequest(1, 4)).getContent().size());
  }

  @Test
  public void shouldSearchByProgramId() {
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

    Page<SupervisoryNode> result = supervisoryNodeRepository
        .search(null, null, geographicZone.getId(), null, null, null, null);

    assertEquals(1, result.getTotalElements());
    assertEquals(node, result.getContent().get(0));
  }
}
