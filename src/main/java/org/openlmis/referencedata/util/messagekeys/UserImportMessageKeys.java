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

public class UserImportMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, USER, IMPORT);
  private static final String FOR_ALL_USERS_SUFFIX_MSG = "forAllUsers";
  private static final String FOR_ALL_ENTRIES_SUFFIX_MSG = "forAllEntries";
  private static final String INVALID_ROLE_TYPE = "invalidRoleType";
  private static final String MISSING_PROGRAM_SUPERVISION_ROLE = "missingProgramForSupervisionRole";
  private static final String MISSING_WAREHOUSE_FULFILLMENT_ROLE =
      "missingWarehouseForFulfillmentRole";

  public static final String ERROR_USERNAME_REQUIRED_FOR_ALL_USERS =
      join(ERROR, USERNAME, REQUIRED, FOR_ALL_USERS_SUFFIX_MSG);
  public static final String ERROR_FIRST_NAME_REQUIRED_FOR_ALL_USERS =
      join(ERROR, FIRSTNAME, REQUIRED, FOR_ALL_USERS_SUFFIX_MSG);
  public static final String ERROR_LAST_NAME_REQUIRED_FOR_ALL_USERS =
      join(ERROR, LASTNAME, REQUIRED, FOR_ALL_USERS_SUFFIX_MSG);

  public static final String ERROR_USERNAME_DUPLICATED = join(ERROR, USERNAME, DUPLICATED);
  public static final String ERROR_EMAIL_DUPLICATED = join(ERROR, EMAIL, DUPLICATED);

  public static final String ERROR_EMAIL_INVALID_FORMAT = join(ERROR, EMAIL, INVALID, FORMAT);

  public static final String ERROR_USERNAME_TOO_LONG = join(ERROR, USERNAME, TOO, LONG);
  public static final String ERROR_EMAIL_TOO_LONG = join(ERROR, EMAIL, TOO, LONG);
  public static final String ERROR_PHONE_NUMBER_TOO_LONG = join(ERROR, PHONE_NUMBER, TOO, LONG);
  public static final String ERROR_TIMEZONE_TOO_LONG = join(ERROR, TIMEZONE, TOO, LONG);
  public static final String ERROR_JOB_TITLE_TOO_LONG = join(ERROR, JOB_TITLE, TOO, LONG);

  public static final String USER_NOT_FOUND = join(ERROR, USER, NOT_FOUND);
  public static final String ROLE_NOT_FOUND = join(ERROR, ROLE, NOT_FOUND);
  public static final String ERROR_USERNAME_REQUIRED_FOR_ALL_ENTRIES =
      join(ERROR, USERNAME, REQUIRED, FOR_ALL_ENTRIES_SUFFIX_MSG);
  public static final String ERROR_ROLE_REQUIRED_FOR_ALL_ENTRIES =
      join(ERROR, ROLE, REQUIRED, FOR_ALL_ENTRIES_SUFFIX_MSG);
  public static final String ERROR_INVALID_ROLE_TYPE =
      join(ERROR, ROLE, INVALID_ROLE_TYPE);
  public static final String ERROR_MISSING_PROGRAM_FOR_SUPERVISION_ROLE =
      join(ERROR, ROLE, MISSING_PROGRAM_SUPERVISION_ROLE);
  public static final String ERROR_MISSING_WAREHOUSE_FOR_FULFILLMENT_ROLE =
      join(ERROR, ROLE, MISSING_WAREHOUSE_FULFILLMENT_ROLE);
}
