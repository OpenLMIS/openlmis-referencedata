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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.CustomPageImpl;
import org.openlmis.referencedata.dto.UserApiResponseDto;
import org.openlmis.referencedata.dto.UserContactDetailsDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.service.export.UserImportHelper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

@RunWith(MockitoJUnitRunner.class)
public class UserDetailsServiceTest {
  private static final String EXTERNAL_API_URL = "/api/userContactDetails";

  @InjectMocks
  private UserDetailsService userDetailsService;

  @Mock
  private RestOperations restTemplate;

  @Mock
  private AuthService authService;

  @Mock
  private UserImportHelper userImportHelper;

  @Before
  public void setUp() {
    when(authService.obtainAccessToken()).thenReturn("token");
  }

  @Test
  public void shouldCallExternalApiForGettingContactDetails() {
    CustomPageImpl<UserContactDetailsDto.UserContactDetailsApiContract> mockPage =
        new CustomPageImpl<>();

    ResponseEntity<CustomPageImpl<UserContactDetailsDto.UserContactDetailsApiContract>> response =
        ResponseEntity.ok(mockPage);

    when(restTemplate.exchange(
        contains(EXTERNAL_API_URL),
        eq(HttpMethod.GET),
        any(),
        ArgumentMatchers.<ParameterizedTypeReference<
            CustomPageImpl<UserContactDetailsDto.UserContactDetailsApiContract>>>any()
    )).thenReturn(response);

    userDetailsService.getUserContactDetails();

    verify(restTemplate).exchange(
        contains(EXTERNAL_API_URL),
        eq(HttpMethod.GET),
        any(),
        ArgumentMatchers.<ParameterizedTypeReference<
            CustomPageImpl<UserContactDetailsDto.UserContactDetailsApiContract>>>any()
    );
  }

  @Test
  public void shouldCallExternalApiForDeletingContactDetails() {
    Set<UUID> userIds = new HashSet<>(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));

    userDetailsService.deleteUserContactDetailsByUserUuids(userIds);

    verify(restTemplate).exchange(
        contains(EXTERNAL_API_URL),
        eq(HttpMethod.DELETE),
        any(),
        eq(Void.class)
    );
  }

  @Test
  public void shouldPrepareDataAndCallExternalApiForSavingContactDetails() {
    UUID userId = UUID.randomUUID();
    UserDto user = new UserDto();
    user.setId(userId);
    user.setUsername("john");

    UserDto importedDto1 = new UserDto();
    importedDto1.setUsername("paul");
    importedDto1.setId(UUID.randomUUID());

    UserDto importedDto2 = new UserDto();
    importedDto2.setUsername("john");
    importedDto2.setId(userId);

    ArgumentCaptor<List<UserContactDetailsDto.UserContactDetailsApiContract>> userDetailsCaptor =
        ArgumentCaptor.forClass(List.class);

    UserApiResponseDto mockResponse =
        new UserApiResponseDto(Collections.emptyList(), Collections.emptyList());
    when(restTemplate.exchange(
        contains(EXTERNAL_API_URL),
        eq(HttpMethod.PUT),
        any(),
        ArgumentMatchers.<ParameterizedTypeReference<UserApiResponseDto>>any(),
        userDetailsCaptor.capture()
    )).thenReturn(ResponseEntity.ok(mockResponse));
    when(userImportHelper.collectErrorsFromResponse(any(UserApiResponseDto.class), anyList()))
        .thenReturn(Collections.emptyList());
    when(userImportHelper.getSuccessfullyCreatedUsers(anyList(), any(UserApiResponseDto.class)))
        .thenReturn(Collections.emptyList());

    userDetailsService.saveUsersContactDetailsFromFile(Collections.singletonList(user),
        Arrays.asList(importedDto1, importedDto2));

    verify(restTemplate).exchange(
        contains(EXTERNAL_API_URL),
        eq(HttpMethod.PUT),
        any(),
        ArgumentMatchers.<ParameterizedTypeReference<UserApiResponseDto>>any(),
        anyList()
    );
  }
}
