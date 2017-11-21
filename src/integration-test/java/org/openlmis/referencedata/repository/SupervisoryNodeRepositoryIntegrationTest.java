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

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

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

  @Override
  CrudRepository<SupervisoryNode, UUID> getRepository() {
    return supervisoryNodeRepository;
  }

  @Before
  public void setUp() {
    String code = "code";

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

    facility = new Facility(code);
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setActive(true);
    facility.setEnabled(true);
    facilityRepository.save(facility);
  }

  @Override
  SupervisoryNode generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    return SupervisoryNode.newSupervisoryNode("node " + instanceNumber,
        "Code #" + instanceNumber, facility);
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
}
