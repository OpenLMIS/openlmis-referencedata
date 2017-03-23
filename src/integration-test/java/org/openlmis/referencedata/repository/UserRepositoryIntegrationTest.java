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
import static org.openlmis.referencedata.domain.RightType.SUPERVISION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.domain.UserBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.UnusedPrivateFiled"})
public class UserRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<User> {

  private static final String EXTRA_DATA_KEY = "color";
  private static final String EXTRA_DATA_VALUE = "orange";
  private static final int TOTAL_USERS = 5;

  @Autowired
  private UserRepository repository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private FacilityRepository facilityRepository;
  
  @Autowired
  private RightRepository rightRepository;
  
  @Autowired
  private RoleRepository roleRepository;
  
  @Autowired
  private ProgramRepository programRepository;
  
  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  private List<User> users;
  
  private ObjectMapper mapper = new ObjectMapper();

  UserRepository getRepository() {
    return this.repository;
  }

  User generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    return new UserBuilder("user" + instanceNumber, "Test", "User", instanceNumber + "@mail.com")
        .setTimezone("UTC")
        .setHomeFacility(generateFacility())
        .setActive(true)
        .setVerified(true)
        .setLoginRestricted(false)
        .createUser();
  }

  @Before
  public void setUp() {

    users = new ArrayList<>();

    for (int usersCount = 0; usersCount < TOTAL_USERS; usersCount++) {
      users.add(repository.save(generateInstance()));
    }
  }

  @Test
  public void testSearchUsersByAllParameters() {
    User user = cloneUser(users.get(0));
    List<User> receivedUsers = repository.searchUsers(
        user.getUsername(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getHomeFacility(),
        user.isActive(),
        user.isVerified(),
        user.isLoginRestricted());

    assertEquals(1, receivedUsers.size());
    assertEquals(
        user.getUsername(),
        receivedUsers.get(0).getUsername());
    assertEquals(
        user.getFirstName(),
        receivedUsers.get(0).getFirstName());
    assertEquals(
        user.getLastName(),
        receivedUsers.get(0).getLastName());
    assertEquals(
        user.getHomeFacility().getId(),
        receivedUsers.get(0).getHomeFacility().getId());
    assertEquals(
        user.isActive(),
        receivedUsers.get(0).isActive());
    assertEquals(
        user.isVerified(),
        receivedUsers.get(0).isVerified());
    assertEquals(
        user.isLoginRestricted(),
        receivedUsers.get(0).isLoginRestricted());
  }

  @Test
  public void searchUsersWithAllParametersNullShouldReturnAllUsers() {
    List<User> receivedUsers = repository.searchUsers(
            null, null, null, null, null, null, null, null);

    assertEquals(6, receivedUsers.size());
  }

  @Test
  public void testSearchUsersByFirstNameAndLastNameAndHomeFacility() {
    User user = cloneUser(users.get(0));
    List<User> receivedUsers = repository.searchUsers(
        null,
        user.getFirstName(),
        user.getLastName(),
        null,
        user.getHomeFacility(),
        null,
        null,
        null);

    assertEquals(2, receivedUsers.size());
    for (User receivedUser : receivedUsers) {
      assertEquals(
          user.getFirstName(),
          receivedUser.getFirstName());
      assertEquals(
          user.getLastName(),
          receivedUser.getLastName());
      assertEquals(
          user.getHomeFacility().getId(),
          receivedUser.getHomeFacility().getId());
    }
  }

  @Test
  public void findByExtraDataShouldFindDataWhenParametersMatch() throws JsonProcessingException {
    //given
    Map<String, String> extraData = Collections.singletonMap(EXTRA_DATA_KEY, EXTRA_DATA_VALUE);
    String extraDataJson = mapper.writeValueAsString(extraData);
    User expectedUser = repository.findOneByUsername("user1");
    expectedUser.setExtraData(extraData);
    repository.save(expectedUser);

    //when
    List<User> extraDataUsers = repository.findByExtraData(extraDataJson);

    //then
    assertEquals(1, extraDataUsers.size());

    User user = extraDataUsers.get(0);
    assertEquals(expectedUser.getUsername(), user.getUsername());
    assertEquals(expectedUser.getFirstName(), user.getFirstName());
    assertEquals(expectedUser.getLastName(), user.getLastName());
    assertEquals(expectedUser.getEmail(), user.getEmail());
    assertEquals(expectedUser.getTimezone(), user.getTimezone());
    assertEquals(expectedUser.getHomeFacility().getId(), user.getHomeFacility().getId());
    assertEquals(expectedUser.isActive(), user.isActive());
    assertEquals(expectedUser.isVerified(), user.isVerified());
    assertEquals(expectedUser.isLoginRestricted(), user.isLoginRestricted());
    assertEquals(expectedUser.getExtraData(), user.getExtraData());
  }

  @Test
  public void findByExtraDataShouldNotFindDataWhenParametersDoNotMatch() {
    //given
    String otherExtraDataJson = "{\"" + EXTRA_DATA_KEY + "\":\"blue\"}";

    //when
    List<User> extraDataUsers = repository.findByExtraData(otherExtraDataJson);

    //then
    assertEquals(0, extraDataUsers.size());
  }

  @Test
  public void findSupervisingUsersByShouldOnlyFindMatchingUsers() {
    //given
    Right right = Right.newRight("right", SUPERVISION);
    rightRepository.save(right);

    Role role = Role.newRole("role", right);
    roleRepository.save(role);

    Program program = new Program("P1");
    programRepository.save(program);

    SupervisoryNode supervisoryNode = SupervisoryNode.newSupervisoryNode("SN1", generateFacility());
    supervisoryNodeRepository.save(supervisoryNode);

    User supervisingUser = repository.findOneByUsername("user1");
    supervisingUser.assignRoles(new SupervisionRoleAssignment(role, supervisingUser, program,
        supervisoryNode));
    repository.save(supervisingUser);

    //when
    Set<User> supervisingUsers = repository.findSupervisingUsersBy(right, supervisoryNode, 
        program);

    //then
    assertEquals(1, supervisingUsers.size());
    assertEquals(supervisingUser, supervisingUsers.iterator().next());
  }

  private User cloneUser(User user) {
    int instanceNumber = this.getNextInstanceNumber();
    User clonedUser = new UserBuilder(user.getUsername() + instanceNumber,
        user.getFirstName(), user.getLastName(), instanceNumber + "@mail.com")
        .setTimezone("UTC")
        .setHomeFacility(user.getHomeFacility())
        .setActive(user.isActive())
        .setVerified(user.isVerified())
        .createUser();
    repository.save(clonedUser);
    return clonedUser;
  }

  private Facility generateFacility() {
    Integer instanceNumber = this.getNextInstanceNumber();
    GeographicLevel geographicLevel = generateGeographicLevel();
    GeographicZone geographicZone = generateGeographicZone(geographicLevel);
    FacilityType facilityType = generateFacilityType();
    Facility facility = new Facility("FacilityCode" + instanceNumber);
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setName("FacilityName" + instanceNumber);
    facility.setDescription("FacilityDescription" + instanceNumber);
    facility.setActive(true);
    facility.setEnabled(true);
    facilityRepository.save(facility);
    return facility;
  }

  private GeographicLevel generateGeographicLevel() {
    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode("GeographicLevel" + this.getNextInstanceNumber());
    geographicLevel.setLevelNumber(1);
    geographicLevelRepository.save(geographicLevel);
    return geographicLevel;
  }

  private GeographicZone generateGeographicZone(GeographicLevel geographicLevel) {
    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode("GeographicZone" + this.getNextInstanceNumber());
    geographicZone.setLevel(geographicLevel);
    geographicZoneRepository.save(geographicZone);
    return geographicZone;
  }

  private FacilityType generateFacilityType() {
    FacilityType facilityType = new FacilityType();
    facilityType.setCode("FacilityType" + this.getNextInstanceNumber());
    facilityTypeRepository.save(facilityType);
    return facilityType;
  }
}
