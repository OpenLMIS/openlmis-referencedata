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

package org.openlmis.referencedata.util.messagekeys;

public class AppConfigurationMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, "appConfiguration");
  private static final String APP_NAME = "appName";
  private static final String THEME = "theme";
  private static final String FEATURE_FLAG = "featureFlag";
  private static final String LOGO = "logo";
  private static final String VERSION = "version";

  public static final String ERROR_NULL = join(ERROR, NULL);
  public static final String ERROR_APP_NAME_BLANK = join(ERROR, APP_NAME, "blank");
  public static final String ERROR_APP_NAME_TOO_LONG = join(ERROR, APP_NAME, "tooLong");
  public static final String ERROR_APP_NAME_INVALID = join(ERROR, APP_NAME, INVALID);
  public static final String ERROR_THEME_PRESET_INVALID = join(ERROR, THEME, "preset", INVALID);
  public static final String ERROR_THEME_APPEARANCE_INVALID =
      join(ERROR, THEME, "defaultAppearance", INVALID);
  public static final String ERROR_FEATURE_FLAGS_TOO_MANY = join(ERROR, "featureFlags", "tooMany");
  public static final String ERROR_FEATURE_FLAG_KEY_INVALID =
      join(ERROR, FEATURE_FLAG, "key", INVALID);
  public static final String ERROR_FEATURE_FLAG_VALUE_INVALID =
      join(ERROR, FEATURE_FLAG, "value", INVALID);
  public static final String ERROR_LOGO_EMPTY = join(ERROR, LOGO, EMPTY);
  public static final String ERROR_LOGO_TOO_LARGE = join(ERROR, LOGO, "tooLarge");
  public static final String ERROR_LOGO_TYPE_INVALID = join(ERROR, LOGO, TYPE, INVALID);
  public static final String ERROR_LOGO_NOT_FOUND = join(ERROR, LOGO, NOT_FOUND);
  public static final String ERROR_VERSION_MISMATCH = join(ERROR, VERSION, MISMATCH);
  public static final String ERROR_VERSION_REQUIRED = join(ERROR, VERSION, REQUIRED);
  public static final String ERROR_VERSION_INVALID = join(ERROR, VERSION, INVALID);
}
