package org.openlmis.referencedata.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.RightQuery;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.web.RightService;
import org.openlmis.referencedata.web.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

@RunWith(MockitoJUnitRunner.class)
public class RightServiceTest {

  private static final String RIGHT_NAME = "RIGHT_NAME";

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private RightService rightService;

  private SecurityContext securityContext;
  private OAuth2Authentication trustedClient;
  private OAuth2Authentication userClient;
  private User user;
  
  @Before
  public void setUp() {
    securityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);
    trustedClient = new OAuth2Authentication(mock(OAuth2Request.class), null);
    userClient = new OAuth2Authentication(mock(OAuth2Request.class), mock(Authentication.class));
    user = mock(User.class);
  }
  
  @Test
  public void checkAdminRightShouldAllowTrustedClients() {
    when(securityContext.getAuthentication()).thenReturn(trustedClient);
    
    rightService.checkAdminRight(RIGHT_NAME);
  }
  
  @Test
  public void checkAdminRightShouldAllowUserWhoHasRight() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    when(userClient.getPrincipal()).thenReturn(user);
    when(userRepository.findOneByUsername(any(String.class))).thenReturn(user);
    when(user.hasRight(any(RightQuery.class))).thenReturn(true);

    rightService.checkAdminRight(RIGHT_NAME);
  }
  
  @Test(expected = UnauthorizedException.class)
  public void checkAdminRightShouldThrowUnauthorizedExceptionForUserWhoDoesNotHaveRight() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    when(userClient.getPrincipal()).thenReturn(user);
    when(userRepository.findOneByUsername(any(String.class))).thenReturn(user);
    when(user.hasRight(any(RightQuery.class))).thenReturn(false);

    rightService.checkAdminRight(RIGHT_NAME);
  }
  
  @Test
  public void checkRootAccessShouldAllowTrustedClients() {
    when(securityContext.getAuthentication()).thenReturn(trustedClient);

    rightService.checkRootAccess();
  }

  @Test(expected = UnauthorizedException.class)
  public void checkRootAccessShouldNotAllowUserClients() {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    rightService.checkRootAccess();
  }
}
