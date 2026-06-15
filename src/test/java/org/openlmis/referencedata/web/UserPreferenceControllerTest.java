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

package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.UserPreference;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.UserPreferenceRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.service.RightService;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class UserPreferenceControllerTest {

  private static final String QUANTITY_UNIT_KEY = "quantityUnit";
  private static final String PACKS = "PACKS";
  private static final String DOSES = "DOSES";

  @InjectMocks
  private UserPreferenceController controller;

  @Mock
  private UserPreferenceRepository userPreferenceRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private RightService rightService;

  private final UUID userId = UUID.randomUUID();

  @Test
  public void shouldReturnUserPreferencesAsMap() {
    when(userPreferenceRepository.findAllByUserId(userId)).thenReturn(Arrays.asList(
        new UserPreference(userId, QUANTITY_UNIT_KEY, PACKS),
        new UserPreference(userId, "locale", "en")));

    Map<String, String> result = controller.getUserPreferences(userId);

    assertEquals(PACKS, result.get(QUANTITY_UNIT_KEY));
    assertEquals("en", result.get("locale"));
    verify(rightService).checkAdminRight(RightName.USERS_MANAGE_RIGHT, true, userId);
  }

  @Test
  public void shouldReturnEmptyMapWhenUserHasNoPreferences() {
    when(userPreferenceRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

    assertTrue(controller.getUserPreferences(userId).isEmpty());
  }

  @Test
  public void shouldCreateNewPreferenceOnPut() {
    when(userRepository.existsById(userId)).thenReturn(true);
    when(userPreferenceRepository.findByUserIdAndPreferenceKey(userId, QUANTITY_UNIT_KEY))
        .thenReturn(Optional.empty());
    when(userPreferenceRepository.findAllByUserId(userId)).thenReturn(
        Collections.singletonList(new UserPreference(userId, QUANTITY_UNIT_KEY, PACKS)));

    Map<String, String> result = controller.saveUserPreferences(userId,
        Collections.singletonMap(QUANTITY_UNIT_KEY, PACKS));

    assertEquals(PACKS, result.get(QUANTITY_UNIT_KEY));
    verify(rightService).checkAdminRight(RightName.USERS_MANAGE_RIGHT, true, userId);

    ArgumentCaptor<UserPreference> captor = ArgumentCaptor.forClass(UserPreference.class);
    verify(userPreferenceRepository).save(captor.capture());
    assertEquals(QUANTITY_UNIT_KEY, captor.getValue().getPreferenceKey());
    assertEquals(PACKS, captor.getValue().getPreferenceValue());
  }

  @Test
  public void shouldUpdateExistingPreferenceOnPut() {
    UserPreference existing = new UserPreference(userId, QUANTITY_UNIT_KEY, DOSES);
    when(userRepository.existsById(userId)).thenReturn(true);
    when(userPreferenceRepository.findByUserIdAndPreferenceKey(userId, QUANTITY_UNIT_KEY))
        .thenReturn(Optional.of(existing));
    when(userPreferenceRepository.findAllByUserId(userId))
        .thenReturn(Collections.singletonList(existing));

    controller.saveUserPreferences(userId, Collections.singletonMap(QUANTITY_UNIT_KEY, PACKS));

    assertEquals(PACKS, existing.getPreferenceValue());
    verify(userPreferenceRepository).save(existing);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowNotFoundWhenUserDoesNotExistOnPut() {
    when(userRepository.existsById(userId)).thenReturn(false);

    controller.saveUserPreferences(userId, Collections.singletonMap(QUANTITY_UNIT_KEY, PACKS));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectBlankValueOnPut() {
    when(userRepository.existsById(userId)).thenReturn(true);

    controller.saveUserPreferences(userId, Collections.singletonMap(QUANTITY_UNIT_KEY, ""));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectBlankKeyOnPut() {
    when(userRepository.existsById(userId)).thenReturn(true);

    controller.saveUserPreferences(userId, Collections.singletonMap("", PACKS));
  }

  @Test
  public void shouldUpsertEachProvidedKeyOnPut() {
    when(userRepository.existsById(userId)).thenReturn(true);
    when(userPreferenceRepository.findByUserIdAndPreferenceKey(any(UUID.class), any(String.class)))
        .thenReturn(Optional.empty());
    when(userPreferenceRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

    Map<String, String> body = new HashMap<>();
    body.put(QUANTITY_UNIT_KEY, PACKS);
    body.put("locale", "fr");

    controller.saveUserPreferences(userId, body);

    verify(userPreferenceRepository, times(2)).save(any(UserPreference.class));
  }

  @Test
  public void shouldAcceptKeyAndValueAtMaxLength() {
    String maxLength = stringOfLength(255);
    when(userRepository.existsById(userId)).thenReturn(true);
    when(userPreferenceRepository.findByUserIdAndPreferenceKey(userId, maxLength))
        .thenReturn(Optional.empty());
    when(userPreferenceRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

    controller.saveUserPreferences(userId, Collections.singletonMap(maxLength, maxLength));

    verify(userPreferenceRepository).save(any(UserPreference.class));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectKeyExceedingMaxLength() {
    when(userRepository.existsById(userId)).thenReturn(true);

    controller.saveUserPreferences(
        userId, Collections.singletonMap(stringOfLength(256), PACKS));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldRejectValueExceedingMaxLength() {
    when(userRepository.existsById(userId)).thenReturn(true);

    controller.saveUserPreferences(
        userId, Collections.singletonMap(QUANTITY_UNIT_KEY, stringOfLength(256)));
  }

  private static String stringOfLength(int length) {
    char[] chars = new char[length];
    Arrays.fill(chars, 'x');
    return new String(chars);
  }
}
