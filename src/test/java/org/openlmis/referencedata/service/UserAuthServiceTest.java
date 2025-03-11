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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.dto.UserApiResponseDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.service.export.UserImportHelper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

@RunWith(MockitoJUnitRunner.class)
public class UserAuthServiceTest {
  private static final String EXTERNAL_API_URL = "/api/users/auth/batch";

  @InjectMocks
  private UserAuthService userAuthService;

  @Mock
  private RestOperations restTemplate;

  @Mock
  private AuthService authService;

  @Mock
  private UserImportHelper userImportHelper;

  @Before
  public void setUp() {
    when(authService.obtainAccessToken()).thenReturn("token");
    when(userImportHelper.getDefaultUserPassword()).thenReturn("password");
  }

  @Test
  public void shouldCallExternalApiForDeletingAuthUsers() {
    Set<UUID> userIds = new HashSet<>(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));

    userAuthService.deleteAuthUsersByUserUuids(userIds);

    verify(restTemplate).exchange(
        contains(EXTERNAL_API_URL),
        eq(HttpMethod.DELETE),
        any(),
        eq(Void.class)
    );
  }

  @Test
  public void shouldPrepareDataAndCallExternalApiForSavingAuthDetails() {
    UserApiResponseDto mockResponse =
        new UserApiResponseDto(Collections.emptyList(), Collections.emptyList());
    when(restTemplate.exchange(
        contains(EXTERNAL_API_URL),
        eq(HttpMethod.POST),
        any(),
        ArgumentMatchers.<ParameterizedTypeReference<UserApiResponseDto>>any()
    )).thenReturn(ResponseEntity.ok(mockResponse));

    UserDto user = new UserDto();
    user.setId(UUID.randomUUID());
    user.setUsername("john");
    List<ImportResponseDto.ErrorDetails> errors = new ArrayList<>();

    userAuthService.saveUserAuthDetailsFromFile(Collections.singletonList(user), errors);

    verify(restTemplate).exchange(
        contains(EXTERNAL_API_URL),
        eq(HttpMethod.POST),
        any(),
        ArgumentMatchers.<ParameterizedTypeReference<UserApiResponseDto>>any()
    );
  }
}
