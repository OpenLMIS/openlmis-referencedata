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


import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.openlmis.referencedata.domain.RightType.GENERAL_ADMIN;
import static org.openlmis.referencedata.domain.RightType.ORDER_FULFILLMENT;
import static org.openlmis.referencedata.domain.RightType.REPORTS;
import static org.openlmis.referencedata.domain.RightType.SUPERVISION;
import static org.powermock.api.mockito.PowerMockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openlmis.referencedata.domain.DirectRoleAssignment;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.domain.UserBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.UnusedPrivateFiled"})
public class UserRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<User> {

  private static final String EXTRA_DATA_KEY = "color";
  private static final String EXTRA_DATA_VALUE = "orange";
  private static final int TOTAL_USERS = 5;

  private static final String USER_1 = "user1";
  private static final String USER_2 = "user2";

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

  @Mock
  private Pageable pageable;

  private List<User> users;
  
  private ObjectMapper mapper = new ObjectMapper();

  UserRepository getRepository() {
    return this.repository;
  }

  User generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    return new UserBuilder("user" + instanceNumber, "Test", "User", instanceNumber + "@mail.com")
        .setTimezone("UTC")
        .setHomeFacilityId(generateFacility(instanceNumber).getId())
        .setActive(true)
        .setVerified(true)
        .setLoginRestricted(false)
        .createUser();
  }

  @Before
  public void setUp() {

    users = new ArrayList<>();

    for (int usersCount = 0; usersCount < TOTAL_USERS - 1; usersCount++) {
      users.add(repository.save(generateInstance()));
    }

    when(pageable.getPageNumber()).thenReturn(0);
    when(pageable.getPageSize()).thenReturn(10);
  }

  @Test
  public void testSearchUsersByAllParameters() {
    User user = cloneUser(users.get(0));
    Page<User> receivedUsers = repository.searchUsers(
        user.getUsername(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getHomeFacilityId(),
        user.isActive(),
        user.isVerified(),
        user.isLoginRestricted(),
        null,
        pageable);

    assertEquals(1, receivedUsers.getContent().size());
    assertEquals(1, receivedUsers.getTotalElements());
    assertEquals(
        user.getUsername(),
        receivedUsers.getContent().get(0).getUsername());
    assertEquals(
        user.getFirstName(),
        receivedUsers.getContent().get(0).getFirstName());
    assertEquals(
        user.getLastName(),
        receivedUsers.getContent().get(0).getLastName());
    assertEquals(
        user.getHomeFacilityId(),
        receivedUsers.getContent().get(0).getHomeFacilityId());
    assertEquals(
        user.isActive(),
        receivedUsers.getContent().get(0).isActive());
    assertEquals(
        user.isVerified(),
        receivedUsers.getContent().get(0).isVerified());
    assertEquals(
        user.isLoginRestricted(),
        receivedUsers.getContent().get(0).isLoginRestricted());
  }

  @Test
  public void searchUsersWithAllParametersNullShouldReturnAllUsers() {
    Page<User> receivedUsers = repository.searchUsers(
            null, null, null, null, null, null, null, null, null, pageable);

    assertEquals(TOTAL_USERS, receivedUsers.getContent().size());
  }

  @Test
  public void testSearchUsersByFirstNameAndLastNameAndHomeFacility() {
    User user = cloneUser(users.get(0));
    Page<User> receivedUsers = repository.searchUsers(
        null,
        user.getFirstName(),
        user.getLastName(),
        null,
        user.getHomeFacilityId(),
        null,
        null,
        null,
        null,
        pageable);

    assertEquals(2, receivedUsers.getContent().size());
    for (User receivedUser : receivedUsers) {
      assertEquals(
          user.getFirstName(),
          receivedUser.getFirstName());
      assertEquals(
          user.getLastName(),
          receivedUser.getLastName());
      assertEquals(
          user.getHomeFacilityId(),
          receivedUser.getHomeFacilityId());
    }
  }

  @Test
  public void testSearchUsersShouldReturnAllUsersListIfEmptyListIsPassed() {
    Page<User> receivedUsers = repository.searchUsers(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        Collections.emptyList(),
        pageable);

    assertEquals(TOTAL_USERS, receivedUsers.getContent().size());
  }

  @Test
  public void testSearchSortByUsername() {
    Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, "username"));
    when(pageable.getSort()).thenReturn(sort);

    Page<User> receivedUsers = repository.searchUsers(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        pageable
    );

    for (int i = 1; i < receivedUsers.getContent().size(); i++) {
      assertTrue(receivedUsers.getContent().get(i).getUsername()
          .compareTo(receivedUsers.getContent().get(i - 1).getUsername()) > 0);
    }
  }

  @Test
  public void testSearchUsersOnlyByEmail() {
    User user1 = cloneUser(users.get(0));
    user1.setEmail("user1@mail.com");
    repository.save(user1);
    User user2 = cloneUser(users.get(0));
    user2.setEmail("user2@mail.com");
    repository.save(user2);

    Page<User> receivedUsers = repository.searchUsers(
        null,
        null,
        null,
        "user",
        null,
        null,
        null,
        null,
        null,
        pageable);

    assertEquals(2, receivedUsers.getContent().size());

    assertTrue(receivedUsers.getContent().contains(user1));
    assertTrue(receivedUsers.getContent().contains(user2));
  }

  @Test
  public void findByExtraDataShouldFindDataWhenParametersMatch() throws JsonProcessingException {
    //given
    Map<String, String> extraData = Collections.singletonMap(EXTRA_DATA_KEY, EXTRA_DATA_VALUE);
    String extraDataJson = mapper.writeValueAsString(extraData);
    User expectedUser = repository.findOneByUsername(USER_1);
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
    assertEquals(expectedUser.getHomeFacilityId(), user.getHomeFacilityId());
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
    Right right = saveNewRight("right", SUPERVISION);
    Role role = saveNewRole("role", right);
    Program program = saveNewProgram("P1");
    SupervisoryNode supervisoryNode = saveNewSupervisoryNode("SN1", generateFacility(10));

    User supervisingUser = repository.findOneByUsername(USER_1);
    supervisingUser = assignRoleToUser(supervisingUser,
        new SupervisionRoleAssignment(role, supervisingUser, program, supervisoryNode));

    //when
    Set<User> supervisingUsers = repository.findSupervisingUsersBy(right, supervisoryNode, 
        program);

    //then
    assertEquals(1, supervisingUsers.size());
    assertEquals(supervisingUser, supervisingUsers.iterator().next());
  }

  @Test
  public void shouldFindUsersByFulfillmentRights() {
    //given
    Right supervisionRight = saveNewRight("supervisionRight", SUPERVISION);
    Role supervisionRole = saveNewRole("supervisionRole", supervisionRight);
    Program program = saveNewProgram("P1");
    SupervisoryNode supervisoryNode = saveNewSupervisoryNode("SN1", generateFacility(10));

    User supervisingUser = repository.findOneByUsername(USER_1);
    assignRoleToUser(supervisingUser, new SupervisionRoleAssignment(
        supervisionRole, supervisingUser, program, supervisoryNode));

    User supervisingUser2 = repository.findOneByUsername(USER_2);
    assignRoleToUser(supervisingUser2, new SupervisionRoleAssignment(
        supervisionRole, supervisingUser2, program, supervisoryNode));

    Right fulfillmentRight = saveNewRight("fulfillmentRight", ORDER_FULFILLMENT);
    Role fulfillmentRole = saveNewRole("fulfillmentRole", fulfillmentRight);
    Facility warehouse = generateFacility(11, "warehouse");

    User warehouseClerk = repository.findOneByUsername("user3");
    warehouseClerk = assignRoleToUser(warehouseClerk,
        new FulfillmentRoleAssignment(fulfillmentRole, warehouseClerk, warehouse));

    User warehouseClerk2 = repository.findOneByUsername("user4");
    warehouseClerk2 = assignRoleToUser(warehouseClerk2,
        new FulfillmentRoleAssignment(fulfillmentRole, warehouseClerk2, warehouse));

    // when
    Set<User> users = repository.findUsersByFulfillmentRight(fulfillmentRight, warehouse);

    // then
    assertThat(users, hasSize(2));
    assertThat(users, hasItems(warehouseClerk, warehouseClerk2));
  }

  @Test
  public void shouldFindUsersByDirectRole() {
    //given
    Right reportRight = saveNewRight("reportRight", REPORTS);
    Role reportRole = saveNewRole("reportRole", reportRight);
    Right adminRight = saveNewRight("adminRight", GENERAL_ADMIN);
    Role adminRole = saveNewRole("adminRole", adminRight);

    User user1 = repository.findOneByUsername(USER_1);
    User user2 = repository.findOneByUsername(USER_2);

    user1.assignRoles(new DirectRoleAssignment(reportRole, user1),
        new DirectRoleAssignment(adminRole, user1));
    user2.assignRoles(new DirectRoleAssignment(reportRole, user2));

    // when
    Set<User> reportUsers = repository.findUsersByDirectRight(reportRight);
    Set<User> adminUsers = repository.findUsersByDirectRight(adminRight);

    // then
    assertThat(reportUsers, hasSize(2));
    assertThat(reportUsers, hasItems(user1, user2));
    assertThat(adminUsers, hasSize(1));
    assertThat(adminUsers, hasItem(user1));
  }

  @Test
  public void shouldFindAllByIds() {
    // given users I want
    User user1 = generateInstance();
    user1 = repository.save(user1);
    User user2 = generateInstance();
    user2 = repository.save(user2);

    // given a user I don't want
    repository.save(generateInstance());

    // when
    Set<UUID> ids = Sets.newHashSet(user1.getId(), user2.getId());
    Page<User> found = repository.findAllByIds(ids, pageable);

    // then
    assertEquals(2, found.getContent().size());
    assertThat(found.getContent(), hasItems(user1, user2));
  }

  private User cloneUser(User user) {
    int instanceNumber = this.getNextInstanceNumber();
    User clonedUser = new UserBuilder(user.getUsername() + instanceNumber,
        user.getFirstName(), user.getLastName(), instanceNumber + "@mail.com")
        .setTimezone("UTC")
        .setHomeFacilityId(user.getHomeFacilityId())
        .setActive(user.isActive())
        .setVerified(user.isVerified())
        .createUser();
    repository.save(clonedUser);
    return clonedUser;
  }

  private Facility generateFacility(int instanceNumber) {
    return generateFacility(instanceNumber, "FacilityCode" + instanceNumber);
  }

  private Facility generateFacility(int instanceNumber, String type) {
    GeographicLevel geographicLevel = generateGeographicLevel(instanceNumber);
    GeographicZone geographicZone = generateGeographicZone(geographicLevel,
        instanceNumber);
    FacilityType facilityType = generateFacilityType(type);
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

  private GeographicLevel generateGeographicLevel(int instanceNumber) {
    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode("GeographicLevel" + instanceNumber);
    geographicLevel.setLevelNumber(1);
    geographicLevelRepository.save(geographicLevel);
    return geographicLevel;
  }

  private GeographicZone generateGeographicZone(GeographicLevel geographicLevel,
                                                int instanceNumber) {
    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode("GeographicZone" + instanceNumber);
    geographicZone.setLevel(geographicLevel);
    geographicZoneRepository.save(geographicZone);
    return geographicZone;
  }

  private FacilityType generateFacilityType(String type) {
    if ("warehouse".equals(type)) {
      return facilityTypeRepository.findOneByCode(type);
    }
    FacilityType facilityType = new FacilityType();
    facilityType.setCode(type);
    facilityTypeRepository.save(facilityType);
    return facilityType;
  }

  private Right saveNewRight(String name, RightType type) {
    Right right = Right.newRight(name, type);
    return rightRepository.save(right);
  }

  private Role saveNewRole(String name, Right right) {
    Role role = Role.newRole(name, right);
    return roleRepository.save(role);
  }

  private Program saveNewProgram(String code) {
    Program program = new Program(code);
    return programRepository.save(program);
  }

  private SupervisoryNode saveNewSupervisoryNode(String code, Facility facility) {
    SupervisoryNode supervisoryNode = SupervisoryNode.newSupervisoryNode(code, facility);
    return supervisoryNodeRepository.save(supervisoryNode);
  }

  private User assignRoleToUser(User user, RoleAssignment assignment) {
    user.assignRoles(assignment);
    return repository.save(user);
  }
}
