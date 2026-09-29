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

package org.openlmis.referencedata.validate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.openlmis.referencedata.dto.AppConfigurationDto;
import org.openlmis.referencedata.util.messagekeys.AppConfigurationMessageKeys;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class AppConfigurationValidator implements BaseValidator {
  static final int MAX_APP_NAME_LENGTH = 64;
  static final int MAX_FEATURE_FLAGS = 100;
  static final int MAX_FEATURE_FLAG_VALUE_LENGTH = 64;

  private static final Pattern PRESET = Pattern.compile("^[a-z0-9-]{1,32}$");
  private static final Pattern FLAG_KEY = Pattern.compile("^[A-Z][A-Z0-9_]{0,63}$");
  private static final Set<String> APPEARANCES =
      new HashSet<>(Arrays.asList("light", "dark", "system"));

  private static final String APP_NAME = "appName";
  private static final String PRESET_FIELD = "theme.preset";
  private static final String APPEARANCE_FIELD = "theme.defaultAppearance";
  private static final String FEATURE_FLAGS = "featureFlags";

  @Override
  public boolean supports(Class<?> clazz) {
    return AppConfigurationDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    verifyArguments(target, errors, AppConfigurationMessageKeys.ERROR_NULL);
    AppConfigurationDto dto = (AppConfigurationDto) target;

    validateAppName(dto.getAppName(), errors);
    validateTheme(dto.getTheme(), errors);
    validateFeatureFlags(dto.getFeatureFlags(), errors);
  }

  private void validateAppName(String appName, Errors errors) {
    if (appName == null) {
      return;
    }
    String trimmed = appName.trim();
    if (trimmed.isEmpty()) {
      rejectValue(errors, APP_NAME, AppConfigurationMessageKeys.ERROR_APP_NAME_BLANK);
    } else if (trimmed.length() > MAX_APP_NAME_LENGTH) {
      rejectValue(errors, APP_NAME, AppConfigurationMessageKeys.ERROR_APP_NAME_TOO_LONG);
    } else if (trimmed.chars().anyMatch(Character::isISOControl)) {
      rejectValue(errors, APP_NAME, AppConfigurationMessageKeys.ERROR_APP_NAME_INVALID);
    }
  }

  private void validateTheme(AppConfigurationDto.ThemeDto theme, Errors errors) {
    if (theme == null) {
      return;
    }
    if (theme.getPreset() != null && !PRESET.matcher(theme.getPreset()).matches()) {
      rejectValue(errors, PRESET_FIELD, AppConfigurationMessageKeys.ERROR_THEME_PRESET_INVALID);
    }
    if (theme.getDefaultAppearance() != null
        && !APPEARANCES.contains(theme.getDefaultAppearance())) {
      rejectValue(errors, APPEARANCE_FIELD,
          AppConfigurationMessageKeys.ERROR_THEME_APPEARANCE_INVALID);
    }
  }

  private void validateFeatureFlags(Map<String, Object> flags, Errors errors) {
    if (flags == null) {
      return;
    }
    if (flags.size() > MAX_FEATURE_FLAGS) {
      rejectValue(errors, FEATURE_FLAGS, AppConfigurationMessageKeys.ERROR_FEATURE_FLAGS_TOO_MANY);
      return;
    }
    flags.forEach((key, value) -> {
      if (!FLAG_KEY.matcher(key).matches()) {
        rejectValue(errors, FEATURE_FLAGS,
            AppConfigurationMessageKeys.ERROR_FEATURE_FLAG_KEY_INVALID, key);
      } else if (!isValidFlagValue(value)) {
        rejectValue(errors, FEATURE_FLAGS,
            AppConfigurationMessageKeys.ERROR_FEATURE_FLAG_VALUE_INVALID, key);
      }
    });
  }

  private boolean isValidFlagValue(Object value) {
    if (value instanceof Boolean) {
      return true;
    }
    return value instanceof String && ((String) value).length() <= MAX_FEATURE_FLAG_VALUE_LENGTH;
  }
}
