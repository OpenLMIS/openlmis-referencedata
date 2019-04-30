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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RightAssignment;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.NamedResource;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.RightAssignmentDataBuilder;
import org.openlmis.referencedata.testbuilder.RightDataBuilder;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;
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
  private Facility facility;
  private Program program;

  @Override
  RightAssignmentRepository getRepository() {
    return this.repository;
  }

  @Override
  RightAssignment generateInstance() {
    return new RightAssignmentDataBuilder()
        .withUser(user1)
        .withRightName(RIGHT_NAME)
        .buildAsNew();
  }
  
  private User persistUser(UUID userId) {
    User user = new UserDataBuilder().buildAsNew();
    user.setId(userId);
    userRepository.save(user);
    return user;
  }

  @Before
  public void setUp() {
    GeographicLevel geographicLevel = new GeographicLevelDataBuilder().buildAsNew();
    geographicLevelRepository.save(geographicLevel);

    GeographicZone geographicZone = new GeographicZoneDataBuilder()
        .withLevel(geographicLevel)
        .buildAsNew();
    geographicZoneRepository.save(geographicZone);

    FacilityType facilityType = new FacilityTypeDataBuilder().buildAsNew();
    facilityTypeRepository.save(facilityType);

    facility = new FacilityDataBuilder()
        .withGeographicZone(geographicZone)
        .withType(facilityType)
        .withoutOperator()
        .buildAsNew();
    facilityRepository.save(facility);

    program = new ProgramDataBuilder().build();
    programRepository.save(program);

    rightRepository.save(new RightDataBuilder()
        .withName(RIGHT_NAME)
        .withType(RightType.GENERAL_ADMIN)
        .buildAsNew());

    userId = UUID.randomUUID();
    user1 = persistUser(userId);

    RightAssignment rightAssignment = this.generateInstance();
    repository.save(rightAssignment);
  }

  @Test
  public void findByUserShouldFindPermissionStrings() {
    // given
    rightRepository.save(new RightDataBuilder()
        .withName(ANOTHER_RIGHT_NAME)
        .withType(RightType.GENERAL_ADMIN)
        .buildAsNew());
    User user2 = persistUser(UUID.randomUUID());
    repository.save(new RightAssignmentDataBuilder()
        .withUser(user2)
        .withRightName(ANOTHER_RIGHT_NAME)
        .buildAsNew());

    // when
    Set<String> foundPermissionStrings = repository.findByUser(userId);

    // then
    assertEquals(1, foundPermissionStrings.size());
    assertEquals(RIGHT_NAME, foundPermissionStrings.iterator().next());
  }

  @Test
  public void findSupervisionProgramsByUserShouldFindPrograms() {
    rightRepository.save(new RightDataBuilder()
        .withName(SUPERVISION_RIGHT_NAME)
        .withType(RightType.SUPERVISION)
        .buildAsNew());
    repository.save(new RightAssignmentDataBuilder()
        .withUser(user1)
        .withRightName(SUPERVISION_RIGHT_NAME)
        .withFacility(facility.getId())
        .withProgram(program.getId())
        .buildAsNew());

    // when
    Set<Program> foundPrograms = programRepository.findSupervisionProgramsByUser(userId);
    
    // then
    assertEquals(1, foundPrograms.size());
    assertEquals(program, foundPrograms.iterator().next());
  }
  
  @Test
  public void findSupervisionFacilitiesByUserShouldFindFacilities() {
    rightRepository.save(new RightDataBuilder()
        .withName(SUPERVISION_RIGHT_NAME)
        .withType(RightType.SUPERVISION)
        .buildAsNew());
    repository.save(new RightAssignmentDataBuilder()
        .withUser(user1)
        .withRightName(SUPERVISION_RIGHT_NAME)
        .withFacility(facility.getId())
        .withProgram(program.getId())
        .buildAsNew());

    // when
    Set<NamedResource> foundFacilities = facilityRepository.findSupervisionFacilitiesByUser(userId);

    // then
    assertEquals(1, foundFacilities.size());
    NamedResource resource = foundFacilities.iterator().next();
    assertEquals(facility.getId(), resource.getId());
    assertEquals(facility.getName(), resource.getName());
  }

  @Test
  public void existsByUserIdAndRightNameShouldBeTrue() {
    // when
    boolean userHasRight = repository.existsByUserIdAndRightName(userId, RIGHT_NAME);

    // then
    assertTrue(userHasRight);
  }

  @Test
  public void existsByUserIdAndRightNameShouldBeFalse() {
    // when
    boolean userHasRight = repository.existsByUserIdAndRightName(userId, SUPERVISION_RIGHT_NAME);

    // then
    assertFalse(userHasRight);
  }
}
