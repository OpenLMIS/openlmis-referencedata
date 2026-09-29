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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.openlmis.referencedata.validate.ValidationTestUtils.assertErrorMessage;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.dto.AppConfigurationDto;
import org.openlmis.referencedata.util.messagekeys.AppConfigurationMessageKeys;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@SuppressWarnings("PMD.TooManyMethods")
public class AppConfigurationValidatorTest {

  private static final String APP_NAME = "appName";
  private static final String PRESET = "theme.preset";
  private static final String APPEARANCE = "theme.defaultAppearance";
  private static final String FEATURE_FLAGS = "featureFlags";

  private final AppConfigurationValidator validator = new AppConfigurationValidator();
  private AppConfigurationDto dto;
  private Errors errors;

  @Before
  public void setUp() {
    dto = new AppConfigurationDto();
    dto.setVersion(0L);
    dto.setAppName("SIGECA");
    dto.setTheme(new AppConfigurationDto.ThemeDto("teal", "dark"));
    Map<String, Object> flags = new LinkedHashMap<>();
    flags.put("BATCH_APPROVE_SCREEN", true);
    flags.put("QUANTITY_UNIT_OPTION", "BOTH");
    dto.setFeatureFlags(flags);
    errors = new BeanPropertyBindingResult(dto, "appConfigurationDto");
  }

  @Test
  public void shouldAcceptValidConfiguration() {
    validator.validate(dto, errors);

    assertFalse(errors.hasErrors());
  }

  @Test
  public void shouldAcceptAllDefaults() {
    dto.setAppName(null);
    dto.setTheme(null);
    dto.setFeatureFlags(null);

    validator.validate(dto, errors);

    assertFalse(errors.hasErrors());
  }

  @Test
  public void shouldAcceptThemeWithNullFields() {
    dto.setTheme(new AppConfigurationDto.ThemeDto(null, null));

    validator.validate(dto, errors);

    assertFalse(errors.hasErrors());
  }

  @Test
  public void shouldRejectBlankAppName() {
    dto.setAppName("   ");

    validator.validate(dto, errors);

    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, APP_NAME, AppConfigurationMessageKeys.ERROR_APP_NAME_BLANK);
  }

  @Test
  public void shouldRejectTooLongAppName() {
    dto.setAppName(StringUtils.repeat('a', 65));

    validator.validate(dto, errors);

    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, APP_NAME, AppConfigurationMessageKeys.ERROR_APP_NAME_TOO_LONG);
  }

  @Test
  public void shouldCountAppNameLengthAfterTrimming() {
    dto.setAppName("  " + StringUtils.repeat('a', 64) + "  ");

    validator.validate(dto, errors);

    assertFalse(errors.hasErrors());
  }

  @Test
  public void shouldRejectAppNameWithControlCharacters() {
    dto.setAppName("Open\nLMIS");

    validator.validate(dto, errors);

    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, APP_NAME, AppConfigurationMessageKeys.ERROR_APP_NAME_INVALID);
  }

  @Test
  public void shouldRejectInvalidPresetName() {
    dto.setTheme(new AppConfigurationDto.ThemeDto("Teal Blue", "dark"));

    validator.validate(dto, errors);

    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, PRESET, AppConfigurationMessageKeys.ERROR_THEME_PRESET_INVALID);
  }

  @Test
  public void shouldAcceptUnknownButWellFormedPresetName() {
    dto.setTheme(new AppConfigurationDto.ThemeDto("ocean-2", null));

    validator.validate(dto, errors);

    assertFalse(errors.hasErrors());
  }

  @Test
  public void shouldRejectUnknownAppearance() {
    dto.setTheme(new AppConfigurationDto.ThemeDto(null, "sepia"));

    validator.validate(dto, errors);

    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, APPEARANCE,
        AppConfigurationMessageKeys.ERROR_THEME_APPEARANCE_INVALID);
  }

  @Test
  public void shouldRejectInvalidFlagKey() {
    dto.getFeatureFlags().put("batchApprove", true);

    validator.validate(dto, errors);

    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, FEATURE_FLAGS,
        AppConfigurationMessageKeys.ERROR_FEATURE_FLAG_KEY_INVALID);
  }

  @Test
  public void shouldRejectFlagValueThatIsNeitherBooleanNorString() {
    dto.getFeatureFlags().put("PAGE_SIZE", 25);

    validator.validate(dto, errors);

    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, FEATURE_FLAGS,
        AppConfigurationMessageKeys.ERROR_FEATURE_FLAG_VALUE_INVALID);
  }

  @Test
  public void shouldRejectTooLongFlagValue() {
    dto.getFeatureFlags().put("QUANTITY_UNIT_OPTION", StringUtils.repeat('A', 65));

    validator.validate(dto, errors);

    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, FEATURE_FLAGS,
        AppConfigurationMessageKeys.ERROR_FEATURE_FLAG_VALUE_INVALID);
  }

  @Test
  public void shouldRejectMoreThanOneHundredFlags() {
    Map<String, Object> flags = new LinkedHashMap<>();
    for (int i = 0; i <= 100; i++) {
      flags.put("FLAG_" + i, true);
    }
    dto.setFeatureFlags(flags);

    validator.validate(dto, errors);

    assertEquals(1, errors.getErrorCount());
    assertErrorMessage(errors, FEATURE_FLAGS,
        AppConfigurationMessageKeys.ERROR_FEATURE_FLAGS_TOO_MANY);
  }
}
