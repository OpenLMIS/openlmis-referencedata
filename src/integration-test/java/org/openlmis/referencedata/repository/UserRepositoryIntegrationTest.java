package org.openlmis.referencedata.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("PMD.TooManyMethods")
public class UserRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<User> {

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
  private RoleRepository roleRepository;

  private List<User> users;
  private List<Role> roles;

  UserRepository getRepository() {
    return this.repository;
  }

  User generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    User user = new User();
    user.setUsername("user" + instanceNumber);
    user.setEmail(instanceNumber + "@mail.com");
    user.setTimezone("UTC");
    user.setFirstName("Test");
    user.setLastName("User");
    user.setHomeFacility(generateFacility());
    user.setActive(true);
    user.setVerified(true);
    return user;
  }

  @Before
  public void setUp() {
    users = new ArrayList<>();
    roles = new ArrayList<>();
    for (int usersCount = 0; usersCount < 5; usersCount++) {
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
            user.getHomeFacility(),
            user.getActive(),
            user.getVerified());

    Assert.assertEquals(1, receivedUsers.size());
    Assert.assertEquals(
            user.getUsername(),
            receivedUsers.get(0).getUsername());
    Assert.assertEquals(
            user.getFirstName(),
            receivedUsers.get(0).getFirstName());
    Assert.assertEquals(
            user.getLastName(),
            receivedUsers.get(0).getLastName());
    Assert.assertEquals(
            user.getHomeFacility().getId(),
            receivedUsers.get(0).getHomeFacility().getId());
    Assert.assertEquals(
            user.getHomeFacility().getActive(),
            receivedUsers.get(0).getActive());
    Assert.assertEquals(
            user.getVerified(),
            receivedUsers.get(0).getVerified());
  }

  @Test
  public void testSearchUsersByAllParametersNull() {
    List<User> receivedUsers = repository.searchUsers(null, null, null, null, null, null);

    Assert.assertEquals(users.size() + 1, receivedUsers.size());
  }

  @Test
  public void testSearchUsersByFirstNameAndLastNameAndHomeFacility() {
    User user = cloneUser(users.get(0));
    List<User> receivedUsers = repository.searchUsers(
            null,
            user.getFirstName(),
            user.getLastName(),
            user.getHomeFacility(),
            null,
            null);

    Assert.assertEquals(2, receivedUsers.size());
    for (User receivedUser : receivedUsers) {
      Assert.assertEquals(
              user.getFirstName(),
              receivedUser.getFirstName());
      Assert.assertEquals(
              user.getLastName(),
              receivedUser.getLastName());
      Assert.assertEquals(
              user.getHomeFacility().getId(),
              receivedUser.getHomeFacility().getId());
    }
  }

  private User cloneUser(User user) {
    int instanceNumber = this.getNextInstanceNumber();
    User clonedUser = new User();
    clonedUser.setUsername(user.getUsername() + instanceNumber);
    clonedUser.setEmail(instanceNumber + "@mail.com");
    clonedUser.setTimezone("UTC");
    clonedUser.setFirstName(user.getFirstName());
    clonedUser.setLastName(user.getLastName());
    clonedUser.setHomeFacility(user.getHomeFacility());
    clonedUser.setActive(user.getActive());
    clonedUser.setVerified(user.getVerified());
    repository.save(clonedUser);
    return clonedUser;
  }

  private Facility generateFacility() {
    Integer instanceNumber = this.getNextInstanceNumber();
    GeographicLevel geographicLevel = generateGeographicLevel();
    GeographicZone geographicZone = generateGeographicZone(geographicLevel);
    FacilityType facilityType = generateFacilityType();
    Facility facility = new Facility();
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setCode("FacilityCode" + instanceNumber);
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
