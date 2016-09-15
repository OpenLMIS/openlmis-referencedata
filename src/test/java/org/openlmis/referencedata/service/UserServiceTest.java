package org.openlmis.referencedata.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.domain.UserBuilder;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.AuthUserRequest;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(BlockJUnit4ClassRunner.class)
@PrepareForTest({UserService.class})
public class UserServiceTest {

  private static final String AUTH_TOKEN = "authToken";

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User user;

  @Before
  public void setUp() {
    user = generateUser();
  }

  @Test
  public void shouldFindUsersIfMatchedRequiredFields() {
    when(userRepository
        .searchUsers(
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getHomeFacility(),
            user.isActive(),
            user.isVerified()))
        .thenReturn(Arrays.asList(user));

    List<User> receivedUsers = userService.searchUsers(user.getUsername(), user.getFirstName(),
        user.getLastName(), user.getHomeFacility(), user.isActive(), user.isVerified());

    assertEquals(1, receivedUsers.size());
    assertEquals(user, receivedUsers.get(0));
  }

  @Test
  public void shouldSaveRequisitionAndAuthUsers() throws Exception {
    when(userRepository.save(user)).thenReturn(user);

    RestTemplate restTemplate = mock(RestTemplate.class);
    whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);

    userService.save(user, AUTH_TOKEN);

    verify(userRepository).save(user);

    ArgumentCaptor<AuthUserRequest> authUserCaptor = ArgumentCaptor.forClass(AuthUserRequest.class);
    verify(restTemplate).postForObject(contains(AUTH_TOKEN), authUserCaptor.capture(), any());

    assertEquals(1, authUserCaptor.getAllValues().size());
    AuthUserRequest authUser = authUserCaptor.getValue();

    assertEquals(user.getUsername(), authUser.getUsername());
    assertEquals(user.getId(), authUser.getReferenceDataUserId());
    assertEquals(user.getEmail(), authUser.getEmail());
    assertTrue(authUser.getEnabled());
    assertEquals("USER", authUser.getRole());
  }

  private User generateUser() {
    return new UserBuilder("kota", "Ala", "ma", "test@mail.com")
        .setId(UUID.randomUUID())
        .setTimezone("UTC")
        .setHomeFacility(mock(Facility.class))
        .setVerified(false)
        .setActive(true)
        .createUser();
  }
}
