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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightAssignment;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.SupportedProgramDataBuilder;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

@SuppressWarnings("PMD.TooManyMethods")
public class ProgramRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Program> {

  private static final String RIGHT_NAME = "rightName";
  
  @Autowired
  ProgramRepository repository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  FacilityRepository facilityRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private RightAssignmentRepository rightAssignmentRepository;

  @Autowired
  private RightRepository rightRepository;

  ProgramRepository getRepository() {
    return this.repository;
  }

  Program generateInstance() {
    return new ProgramDataBuilder()
        .withoutId()
        .build();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldThrowExceptionWhenCodeIsDuplicated() {
    Program program1 = this.generateInstance();
    repository.saveAndFlush(program1);

    Program program2 = this.generateInstance();
    program2.setCode(program1.getCode());
    repository.saveAndFlush(program2);
  }

  @Test
  public void testSkippableEdit() {
    Program testProgram = this.generateInstance();
    testProgram = repository.save(testProgram);
    testProgram = repository.findOne(testProgram.getId());
    assertTrue(testProgram.getPeriodsSkippable());

    testProgram.setPeriodsSkippable(false);
    testProgram = repository.save(testProgram);
    testProgram = repository.findOne(testProgram.getId());
    assertFalse(testProgram.getPeriodsSkippable());
  }

  @Test
  public void testEnableDatePhysicalStockCountCompletedEdit() {
    Program testProgram = this.generateInstance();
    testProgram = repository.save(testProgram);
    assertFalse(testProgram.getEnableDatePhysicalStockCountCompleted());

    testProgram.setEnableDatePhysicalStockCountCompleted(true);
    testProgram = repository.save(testProgram);
    assertTrue(testProgram.getEnableDatePhysicalStockCountCompleted());
  }
  
  @Test
  public void shouldFindByCode() {
    //given
    Program program = this.generateInstance();
    repository.save(program);

    //when
    Program foundProgram = repository.findByCode(program.getCode());

    //then
    assertEquals(program, foundProgram);
  }

  @Test
  public void shouldFindBySimilarName() {
    Program program = this.generateInstance();
    repository.save(program);

    List<Program> foundPrograms = repository.findProgramsByName("Program");

    assertEquals(1, foundPrograms.size());
    assertEquals(program, foundPrograms.get(0));
  }

  @Test
  public void shouldFindBySimilarNameIgnoringCase() {
    Program program = this.generateInstance();
    repository.save(program);

    List<Program> foundPrograms = repository.findProgramsByName("PROGRAM");

    assertEquals(1, foundPrograms.size());
    assertEquals(program, foundPrograms.get(0));
    foundPrograms = repository.findProgramsByName("program");

    assertEquals(1, foundPrograms.size());
    assertEquals(program, foundPrograms.get(0));

    foundPrograms = repository.findProgramsByName("ProGRam");

    assertEquals(1, foundPrograms.size());
    assertEquals(program, foundPrograms.get(0));
  }

  @Test
  public void shouldNotFindByIncorrectSimilarName() {
    Program program = this.generateInstance();
    repository.save(program);

    List<Program> foundPrograms = repository.findProgramsByName("Incorrect Name");

    assertEquals(0, foundPrograms.size());
  }

  @Test
  public void shouldFindProgramsByName() {
    Program program1 = this.generateInstance();
    program1.setName("name1");
    repository.save(program1);
    Program program2 = this.generateInstance();
    program2.setName("name2");
    repository.save(program2);
    Program program3 = this.generateInstance();
    program3.setName("something");
    repository.save(program3);

    List<Program> foundPrograms = repository.findByNameIgnoreCaseContaining("name");

    assertThat(foundPrograms, hasItems(program1, program2));
  }

  @Test
  public void shouldFindProgramsByNameAndIds() {
    Program program1 = this.generateInstance();
    program1.setName("name1");
    repository.save(program1);
    Program program2 = this.generateInstance();
    program2.setName("name2");
    repository.save(program2);
    Program program3 = this.generateInstance();
    program3.setName("something");
    repository.save(program3);

    List<Program> foundPrograms = repository.findByIdInAndNameIgnoreCaseContaining(
        asList(program2.getId(), program3.getId()), "name");

    assertThat(foundPrograms, hasItem(program2));
  }
  
  @Test
  public void findHomeFacilitySupervisionProgramsByUserShouldFindProgram() {
    // given
    Program homeFacilityProgram = this.generateInstance();
    homeFacilityProgram.setActive(true);
    repository.save(homeFacilityProgram);
    
    Facility homeFacility = persistFacilityWithProgram(homeFacilityProgram, true);

    User user = persistUserWithHomeFacilityId(homeFacility.getId());

    persistRightAssignment(user, RIGHT_NAME, homeFacility.getId(), homeFacilityProgram.getId());

    // when
    Set<Program> foundPrograms = repository.findHomeFacilitySupervisionProgramsByUser(user.getId());
    
    // then
    assertEquals(1, foundPrograms.size());
    assertEquals(homeFacilityProgram, foundPrograms.iterator().next());
  }

  @Test
  public void findHomeFacilitySupervisionProgramsByUserShouldNotFindIfProgramNotActive() {
    // given
    Program homeFacilityProgram = this.generateInstance();
    homeFacilityProgram.setActive(false);
    repository.save(homeFacilityProgram);

    Facility homeFacility = persistFacilityWithProgram(homeFacilityProgram, true);

    User user = persistUserWithHomeFacilityId(homeFacility.getId());

    persistRightAssignment(user, RIGHT_NAME, homeFacility.getId(), homeFacilityProgram.getId());

    // when
    Set<Program> foundPrograms = repository.findHomeFacilitySupervisionProgramsByUser(user.getId());

    // then
    assertTrue(foundPrograms.isEmpty());
  }

  @Test
  public void findHomeFacilitySupervisionProgramsByUserShouldNotFindIfSupportNotActive() {
    // given
    Program homeFacilityProgram = this.generateInstance();
    homeFacilityProgram.setActive(true);
    repository.save(homeFacilityProgram);

    Facility homeFacility = persistFacilityWithProgram(homeFacilityProgram, false);

    User user = persistUserWithHomeFacilityId(homeFacility.getId());
    
    persistRightAssignment(user, RIGHT_NAME, homeFacility.getId(), homeFacilityProgram.getId());

    // when
    Set<Program> foundPrograms = repository.findHomeFacilitySupervisionProgramsByUser(user.getId());

    // then
    assertTrue(foundPrograms.isEmpty());
  }

  @Test
  public void findHomeFacilitySupervisionProgramsByUserShouldNotFindIfNoHomeFacilityProgram() {
    // given
    Program homeFacilityProgram = this.generateInstance();
    homeFacilityProgram.setActive(true);
    repository.save(homeFacilityProgram);

    Facility homeFacility = persistFacilityWithProgram(null, true);

    User user = persistUserWithHomeFacilityId(homeFacility.getId());

    persistRightAssignment(user, RIGHT_NAME, homeFacility.getId(), homeFacilityProgram.getId());

    // when
    Set<Program> foundPrograms = repository.findHomeFacilitySupervisionProgramsByUser(user.getId());

    // then
    assertTrue(foundPrograms.isEmpty());
  }

  @Test
  public void findHomeFacilitySupervisionProgramsByUserShouldNotFindIfNoHomeFacility() {
    // given
    Program homeFacilityProgram = this.generateInstance();
    homeFacilityProgram.setActive(true);
    repository.save(homeFacilityProgram);

    User user = persistUserWithHomeFacilityId(null);

    persistRightAssignment(user, RIGHT_NAME, null, null);

    // when
    Set<Program> foundPrograms = repository.findHomeFacilitySupervisionProgramsByUser(user.getId());

    // then
    assertTrue(foundPrograms.isEmpty());
  }

  private User persistUserWithHomeFacilityId(UUID homeFacilityId) {
    User user = new UserDataBuilder()
        .withHomeFacilityId(homeFacilityId)
        .buildAsNew();
    userRepository.save(user);
    return user;
  }
  
  private Facility persistFacilityWithProgram(Program program, boolean isSupportActive) {
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
    if (program != null) {
      SupportedProgram supportedProgram = new SupportedProgramDataBuilder()
          .withFacility(facility)
          .withProgram(program)
          .withActiveFlag(isSupportActive)
          .build();

      facility.addSupportedProgram(supportedProgram);
    }

    facilityRepository.save(facility);

    return facility;
  }
  
  private void persistRightAssignment(User user, String rightName, UUID facilityId,
      UUID programId) {
    rightRepository.save(Right.newRight(rightName, RightType.SUPERVISION));
    rightAssignmentRepository.save(new RightAssignment(user, rightName, facilityId, programId));
  }
}
