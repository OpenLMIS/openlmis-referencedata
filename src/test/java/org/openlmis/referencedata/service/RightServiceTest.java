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

package org.openlmis.referencedata.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.testbuilder.OAuth2AuthenticationDataBuilder.SERVICE_CLIENT_ID;
import static org.openlmis.referencedata.testbuilder.OAuth2AuthenticationDataBuilder.asApiKey;
import static org.openlmis.referencedata.testbuilder.OAuth2AuthenticationDataBuilder.asClient;
import static org.openlmis.referencedata.testbuilder.OAuth2AuthenticationDataBuilder.asService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.RightQuery;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.repository.RightAssignmentRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class RightServiceTest {

  private static final String RIGHT_NAME = "RIGHT_NAME";

  @Mock
  private UserRepository userRepository;

  @Mock
  private RightAssignmentRepository rightAssignmentRepository;

  @InjectMocks
  private RightService rightService;

  private SecurityContext securityContext;
  private OAuth2Authentication trustedClient;
  private OAuth2Authentication userClient;
  private OAuth2Authentication apiKeyClient;
  private User user;
  private UUID userId;
  
  @Before
  public void setUp() {
    securityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);

    user = mock(User.class);
    userId = UUID.randomUUID();
    when(user.getId()).thenReturn(userId);

    trustedClient = asService();
    userClient = asClient(userId);
    apiKeyClient = asApiKey();

    ReflectionTestUtils.setField(rightService, "serviceTokenClientId", SERVICE_CLIENT_ID);
  }
  
  @Test
  public void checkAdminRightShouldAllowTrustedClients() {
    when(securityContext.getAuthentication()).thenReturn(trustedClient);
    
    rightService.checkAdminRight(RIGHT_NAME);
  }

  @Test(expected = UnauthorizedException.class)
  public void checkAdminRightShouldThrowExceptionWhenServiceLevelTokenNotAllowedAndNoUser() {
    when(securityContext.getAuthentication()).thenReturn(trustedClient);

    rightService.checkAdminRight(RIGHT_NAME, false);
  }

  @Test(expected = UnauthorizedException.class)
  public void checkAdminRightShouldThrowExceptionWhenApiKeyNotAllowed() {
    when(securityContext.getAuthentication()).thenReturn(apiKeyClient);

    rightService.checkAdminRight(RIGHT_NAME);
  }

  @Test
  public void checkAdminRightShouldAllowUserWhoHasRight() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    when(rightAssignmentRepository.existsByUserIdAndRightName(user.getId(), RIGHT_NAME))
        .thenReturn(true);

    rightService.checkAdminRight(RIGHT_NAME);
  }

  @Test
  public void checkAdminRightShouldAllowRequesterWithSpecifiedUserId() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    when(userRepository.exists(any(UUID.class))).thenReturn(true);
    when(userRepository.findOne(any(UUID.class))).thenReturn(user);
    when(user.hasRight(any(RightQuery.class))).thenReturn(false);

    rightService.checkAdminRight(RIGHT_NAME, true, user.getId());
  }
  
  @Test(expected = UnauthorizedException.class)
  public void checkAdminRightShouldThrowUnauthorizedExceptionForUserWhoDoesNotHaveRight() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    when(rightAssignmentRepository.existsByUserIdAndRightName(user.getId(), RIGHT_NAME))
        .thenReturn(false);

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

  @Test(expected = UnauthorizedException.class)
  public void checkRootAccessShouldNotAllowApiKeys() {
    when(securityContext.getAuthentication()).thenReturn(apiKeyClient);

    rightService.checkRootAccess();
  }
}
