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

import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightAssignment;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.domain.UserBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class RightAssignmentRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<RightAssignment> {

  private static final String RIGHT_NAME = "aRight";
  private static final String ANOTHER_RIGHT_NAME = "anotherRight";
  private static final String SUPERVISION_RIGHT_NAME = "supervisionRight";
  
  @Autowired
  private RightAssignmentRepository repository;
  
  @Autowired
  private RightRepository rightRepository;
  
  @Autowired
  private UserRepository userRepository;
  
  @Autowired
  private ProgramRepository programRepository;
  
  @Autowired
  private GeographicLevelRepository geographicLevelRepository;
  
  @Autowired
  private GeographicZoneRepository geographicZoneRepository;
  
  @Autowired
  private FacilityTypeRepository facilityTypeRepository;
  
  @Autowired
  private FacilityRepository facilityRepository;

  private User user1;
  private UUID userId;

  @Override
  RightAssignmentRepository getRepository() {
    return this.repository;
  }

  @Override
  RightAssignment generateInstance() {
    return new RightAssignment(user1, RIGHT_NAME);
  }
  
  private User persistUser(int instanceNumber, UUID userId) {
    User user = new UserBuilder(
        "user" + instanceNumber,
        "Test",
        "User",
        instanceNumber + "@mail.com")
        .setTimezone("UTC")
        .setActive(true)
        .setVerified(true)
        .setLoginRestricted(false)
        .createUser();
    user.setId(userId);
    userRepository.save(user);
    return user;
  }

  @Before
  public void setUp() {
    rightRepository.save(Right.newRight(RIGHT_NAME, RightType.GENERAL_ADMIN));
    
    userId = UUID.randomUUID();
    user1 = persistUser(getNextInstanceNumber(), userId);
    
    RightAssignment rightAssignment = this.generateInstance();
    repository.save(rightAssignment);
  }

  @Test
  public void findByUserShouldFindPermissionStrings() {
    // given
    rightRepository.save(Right.newRight(ANOTHER_RIGHT_NAME, RightType.GENERAL_ADMIN));
    User user2 = persistUser(getNextInstanceNumber(), UUID.randomUUID());
    repository.save(new RightAssignment(user2, ANOTHER_RIGHT_NAME));

    // when
    Set<String> foundPermissionStrings = repository.findByUser(userId);

    // then
    assertEquals(1, foundPermissionStrings.size());
    assertEquals(RIGHT_NAME, foundPermissionStrings.iterator().next());
  }

  @Test
  public void findSupervisionProgramsByUserShouldFindPrograms() {
    // given
    Program program = persistProgram();
    Facility facility = persistFacility();
    
    rightRepository.save(Right.newRight(SUPERVISION_RIGHT_NAME, RightType.SUPERVISION));
    repository.save(new RightAssignment(
        user1, SUPERVISION_RIGHT_NAME, facility.getId(), program.getId()));

    // when
    Set<Program> foundPrograms = programRepository.findSupervisionProgramsByUser(userId);
    
    // then
    assertEquals(1, foundPrograms.size());
    assertEquals(program, foundPrograms.iterator().next());
  }
  
  @Test
  public void findSupervisionFacilitiesByUserShouldFindFacilities() {
    // given
    Program program = persistProgram();
    Facility facility = persistFacility();

    rightRepository.save(Right.newRight(SUPERVISION_RIGHT_NAME, RightType.SUPERVISION));
    repository.save(new RightAssignment(
        user1, SUPERVISION_RIGHT_NAME, facility.getId(), program.getId()));

    // when
    Set<Facility> foundFacilities = facilityRepository.findSupervisionFacilitiesByUser(userId);

    // then
    assertEquals(1, foundFacilities.size());
    assertEquals(facility, foundFacilities.iterator().next());
  }
  
  private Program persistProgram() {
    Program program = new Program("P1");
    program.setPeriodsSkippable(true);
    programRepository.save(program);
    return program;
  }
  
  private Facility persistFacility() {
    GeographicLevel geographicLevel = new GeographicLevel("GL1", 1);
    geographicLevelRepository.save(geographicLevel);
    
    GeographicZone geographicZone = new GeographicZone("G1", geographicLevel);
    geographicZoneRepository.save(geographicZone);
    
    FacilityType facilityType = new FacilityType("FT1");
    facilityTypeRepository.save(facilityType);

    Facility facility = new Facility("F1");
    facility.setActive(true);
    facility.setEnabled(true);
    facility.setGeographicZone(geographicZone);
    facility.setType(facilityType);
    facilityRepository.save(facility);
    return facility;
  }
}
