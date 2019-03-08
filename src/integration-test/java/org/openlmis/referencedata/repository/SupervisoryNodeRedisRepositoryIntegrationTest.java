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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.repository.custom.SupervisoryNodeRedisRepository;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityOperatorDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class SupervisoryNodeRedisRepositoryIntegrationTest {

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private FacilityOperatorRepository facilityOperatorRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private SupervisoryNodeRedisRepository supervisoryNodeRedisRepository;

  private SupervisoryNode supervisoryNode;

  @Before
  public void setUp() {
    GeographicLevel geographicLevel = new GeographicLevelDataBuilder().build();
    geographicLevelRepository.save(geographicLevel);

    GeographicZone geographicZone = new GeographicZoneDataBuilder()
        .withLevel(geographicLevel)
        .build();
    geographicZoneRepository.save(geographicZone);

    FacilityType facilityType = new FacilityTypeDataBuilder().build();
    facilityTypeRepository.save(facilityType);

    FacilityOperator facilityOperator = new FacilityOperatorDataBuilder().build();
    facilityOperatorRepository.save(facilityOperator);

    Facility facility = new FacilityDataBuilder()
        .withGeographicZone(geographicZone)
        .withOperator(facilityOperator)
        .withType(facilityType)
        .build();
    facilityRepository.save(facility);

    supervisoryNode = new SupervisoryNodeDataBuilder()
        .withFacility(facility)
        .build();

    supervisoryNodeRepository.save(supervisoryNode);
    supervisoryNodeRedisRepository.save(supervisoryNode);
  }

  @Test
  @Ignore
  public void shouldSaveAndReturnTrueIfSupervisoryNodeExistsInCacheWithGivenId() {
    UUID supervisoryNodeId = supervisoryNode.getId();

    boolean exists = supervisoryNodeRedisRepository.existsInCache(supervisoryNodeId);

    assertTrue(exists);
  }

  @Test
  @Ignore
  public void shouldFindSupervisoryNodeById() {
    UUID supervisoryNodeId = supervisoryNode.getId();

    SupervisoryNode supervisoryNodeFromCache = supervisoryNodeRedisRepository
        .findById(supervisoryNodeId);

    assertNotNull(supervisoryNodeFromCache);
  }

  @Test
  public void shouldDeleteSupervisoryNode() {
    supervisoryNodeRedisRepository.delete(supervisoryNode);

    assertFalse(supervisoryNodeRedisRepository.existsInCache(supervisoryNode.getId()));
  }

  @Test
  @Ignore
  public void shouldFindSupervisoryNodeInDatabaseAndInCache() {
    SupervisoryNode supervisoryNodeFromDataBase = supervisoryNodeRepository
        .findOne(supervisoryNode.getId());
    assertNotNull(supervisoryNodeFromDataBase);

    SupervisoryNode supervisoryNodeFromCache = supervisoryNodeRedisRepository
        .findById(supervisoryNode.getId());
    assertNotNull(supervisoryNodeFromCache);
  }
}