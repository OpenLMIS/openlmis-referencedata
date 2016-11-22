package org.openlmis.referencedata.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.domain.UserBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        user.getHomeFacility(),
        user.isActive(),
        user.isVerified(),
        user.isLoginRestricted());

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
        user.isActive(),
        receivedUsers.get(0).isActive());
    Assert.assertEquals(
        user.isVerified(),
        receivedUsers.get(0).isVerified());
    Assert.assertEquals(
        user.isLoginRestricted(),
        receivedUsers.get(0).isLoginRestricted());
  }

  @Test
  public void searchUsersWithAllParametersNullShouldReturnAnEmptyList() {
    List<User> receivedUsers = repository.searchUsers(null, null, null, null, null, null, null);

    Assert.assertEquals(0, receivedUsers.size());
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
    Assert.assertEquals(1, extraDataUsers.size());

    User user = extraDataUsers.get(0);
    Assert.assertEquals(expectedUser.getUsername(), user.getUsername());
    Assert.assertEquals(expectedUser.getFirstName(), user.getFirstName());
    Assert.assertEquals(expectedUser.getLastName(), user.getLastName());
    Assert.assertEquals(expectedUser.getEmail(), user.getEmail());
    Assert.assertEquals(expectedUser.getTimezone(), user.getTimezone());
    Assert.assertEquals(expectedUser.getHomeFacility().getId(), user.getHomeFacility().getId());
    Assert.assertEquals(expectedUser.isActive(), user.isActive());
    Assert.assertEquals(expectedUser.isVerified(), user.isVerified());
    Assert.assertEquals(expectedUser.isLoginRestricted(), user.isLoginRestricted());
    Assert.assertEquals(expectedUser.getExtraData(), user.getExtraData());
  }

  @Test
  public void findByExtraDataShouldNotFindDataWhenParametersDoNotMatch() {
    //given
    String otherExtraDataJson = "{\"" + EXTRA_DATA_KEY + "\":\"blue\"}";

    //when
    List<User> extraDataUsers = repository.findByExtraData(otherExtraDataJson);

    //then
    Assert.assertEquals(0, extraDataUsers.size());
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
