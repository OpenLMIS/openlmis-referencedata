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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.testbuilder.OAuth2AuthenticationDataBuilder.API_KEY_PREFIX;
import static org.openlmis.referencedata.testbuilder.OAuth2AuthenticationDataBuilder.SERVICE_CLIENT_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.UnauthorizedException;
import org.openlmis.referencedata.repository.RightAssignmentRepository;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.testbuilder.OAuth2AuthenticationDataBuilder;
import org.openlmis.referencedata.testbuilder.RightDataBuilder;
import org.openlmis.referencedata.testbuilder.RoleDataBuilder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class RightServiceTest {

  private static final String RIGHT_NAME = "RIGHT_NAME";

  @Mock
  private UserRepository userRepository;

  @Mock
  private RightAssignmentRepository rightAssignmentRepository;

  @Mock
  private RightRepository rightRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private AuthenticationHelper authenticationHelper;

  @InjectMocks
  private RightService rightService;

  private SecurityContext securityContext;
  private OAuth2Authentication trustedClient;
  private OAuth2Authentication userClient;
  private OAuth2Authentication apiKeyClient;
  private User user;
  private UUID userId;
  private Right right;

  @Before
  public void setUp() {
    securityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);

    user = mock(User.class);
    userId = UUID.randomUUID();
    when(user.getId()).thenReturn(userId);

    when(authenticationHelper.getCurrentUser()).thenReturn(user);

    trustedClient = new OAuth2AuthenticationDataBuilder().buildServiceAuthentication();
    userClient = new OAuth2AuthenticationDataBuilder().withUserId(userId).buildUserAuthentication();
    apiKeyClient = new OAuth2AuthenticationDataBuilder().buildApiKeyAuthentication();

    ReflectionTestUtils.setField(rightService, "serviceTokenClientId", SERVICE_CLIENT_ID);
    ReflectionTestUtils.setField(rightService, "apiKeyPrefix", API_KEY_PREFIX);

    String rightName = "right";
    right = new RightDataBuilder().withName(rightName).build();
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
    when(userRepository.existsById(any(UUID.class))).thenReturn(true);

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

  @Test
  public void shouldReturnTrueIfUserHasRight() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    when(rightAssignmentRepository.existsByUserIdAndRightName(user.getId(), RIGHT_NAME))
        .thenReturn(true);

    assertThat(rightService.hasRight(RIGHT_NAME)).isTrue();
  }

  @Test
  public void shouldReturnFalseIfUserHasNoRight() {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    when(rightAssignmentRepository.existsByUserIdAndRightName(user.getId(), RIGHT_NAME))
        .thenReturn(false);

    assertThat(rightService.hasRight(RIGHT_NAME)).isFalse();
  }

  @Test
  public void shouldDeleteRight() {
    when(securityContext.getAuthentication()).thenReturn(trustedClient);
    when(rightRepository.findById(right.getId())).thenReturn(Optional.ofNullable(right));
    when(roleRepository.findAllByRightId(right.getId())).thenReturn(Collections.emptyList());

    rightService.deleteRight(right.getId());

    verify(rightAssignmentRepository).deleteAllByRightName(right.getName());
    verify(rightRepository).delete(right);
  }

  @Test
  public void shouldDeleteRightAndUnassignRoles() {
    String role1Name = "role1";
    String role2Name = "role2";
    Role role1 = new RoleDataBuilder().withName(role1Name).withRights(right).build();
    Role role2 = new RoleDataBuilder().withName(role2Name).withRights(right).build();
    List<Role> roles = new ArrayList<>();
    roles.add(role1);
    roles.add(role2);

    when(securityContext.getAuthentication()).thenReturn(trustedClient);
    when(rightRepository.findById(right.getId())).thenReturn(Optional.ofNullable(right));
    when(roleRepository.findAllByRightId(right.getId())).thenReturn(roles);

    rightService.deleteRight(right.getId());

    ArgumentCaptor<List<Role>> rolesCaptor = ArgumentCaptor.forClass(List.class);
    verify(roleRepository).saveAll(rolesCaptor.capture());

    List<Role> updatedRoles = rolesCaptor.getValue();

    for (Role role : updatedRoles) {
      assertFalse(role.getRights().contains(right), "Right should be removed from the role");
    }

    verify(rightAssignmentRepository).deleteAllByRightName(right.getName());
    verify(rightRepository).delete(right);
  }

  @Test(expected = NotFoundException.class)
  public void shouldNotDeleteNonExistingRight() {
    when(securityContext.getAuthentication()).thenReturn(trustedClient);
    when(rightRepository.findById(right.getId())).thenReturn(Optional.empty());

    rightService.deleteRight(right.getId());
  }
}
