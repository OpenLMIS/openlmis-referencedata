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

public abstract class UserPreferenceMessageKeys extends MessageKeys {
  private static final String USER_PREFERENCE = "userPreference";
  private static final String ERROR = join(SERVICE_ERROR, USER_PREFERENCE);
  private static final String KEY = "key";
  private static final String VALUE = "value";

  public static final String ERROR_KEY_INVALID = join(ERROR, KEY, INVALID);
  public static final String ERROR_VALUE_INVALID = join(ERROR, VALUE, INVALID);
}
