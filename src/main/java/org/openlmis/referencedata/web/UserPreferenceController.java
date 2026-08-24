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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.domain.UserPreference;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.UserPreferenceRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.messagekeys.UserMessageKeys;
import org.openlmis.referencedata.util.messagekeys.UserPreferenceMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@Transactional
public class UserPreferenceController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserPreferenceController.class);

  private static final int MAX_LENGTH = 255;

  @Autowired
  private UserPreferenceRepository userPreferenceRepository;

  @Autowired
  private UserRepository userRepository;

  /**
   * Returns all of the given user's preferences as a key-value map. Accessible to the user
   * themselves or to an administrator with the USERS_MANAGE right.
   *
   * @param userId the user whose preferences to return
   * @return a map of preference key to value (empty when the user has none)
   */
  @GetMapping("/users/{userId}/preferences")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, String> getUserPreferences(@PathVariable("userId") UUID userId) {
    Profiler profiler = new Profiler("GET_USER_PREFERENCES");
    profiler.setLogger(LOGGER);

    checkAdminRight(RightName.USERS_MANAGE_RIGHT, true, userId, profiler);

    profiler.start("FIND_PREFERENCES");
    Map<String, String> preferences = userPreferenceRepository.findAllByUserId(userId)
        .stream()
        .collect(Collectors.toMap(
            UserPreference::getPreferenceKey, UserPreference::getPreferenceValue));

    profiler.stop().log();
    return preferences;
  }

  /**
   * Upserts the given preferences for the user (each provided key is created or updated, others are
   * left untouched). Accessible to the user themselves or to an administrator with the USERS_MANAGE
   * right.
   *
   * @param userId      the user whose preferences to update
   * @param preferences the preference key-value pairs to store
   * @return the user's full set of preferences after the update
   */
  @PutMapping("/users/{userId}/preferences")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, String> saveUserPreferences(@PathVariable("userId") UUID userId,
      @RequestBody Map<String, String> preferences) {
    Profiler profiler = new Profiler("SAVE_USER_PREFERENCES");
    profiler.setLogger(LOGGER);

    checkAdminRight(RightName.USERS_MANAGE_RIGHT, true, userId, profiler);

    profiler.start("CHECK_USER_EXISTS");
    if (!userRepository.existsById(userId)) {
      profiler.stop().log();
      throw new NotFoundException(UserMessageKeys.ERROR_NOT_FOUND);
    }

    profiler.start("VALIDATE_AND_SAVE");
    preferences.forEach((key, value) -> savePreference(userId, key, value));

    profiler.start("FIND_PREFERENCES");
    Map<String, String> result = userPreferenceRepository.findAllByUserId(userId)
        .stream()
        .collect(Collectors.toMap(
            UserPreference::getPreferenceKey, UserPreference::getPreferenceValue));

    profiler.stop().log();
    return result;
  }

  private void savePreference(UUID userId, String key, String value) {
    if (isBlank(key) || key.length() > MAX_LENGTH) {
      throw new ValidationMessageException(UserPreferenceMessageKeys.ERROR_KEY_INVALID);
    }
    if (isBlank(value) || value.length() > MAX_LENGTH) {
      throw new ValidationMessageException(UserPreferenceMessageKeys.ERROR_VALUE_INVALID);
    }

    UserPreference preference = userPreferenceRepository
        .findByUserIdAndPreferenceKey(userId, key)
        .orElseGet(() -> new UserPreference(userId, key, value));
    preference.setPreferenceValue(value);

    userPreferenceRepository.save(preference);
  }
}
