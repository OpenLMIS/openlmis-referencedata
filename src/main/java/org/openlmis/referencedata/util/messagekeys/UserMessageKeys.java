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

public abstract class UserMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, USER);
  private static final String RIGHTS = "rights";
  private static final String ASSIGNED_ROLE = "assignedRole";
  private static final String HOME_FACILITY = "homeFacility";
  private static final String RESET_PASSWORD = "resetPassword";
  private static final String CHANGE_PASSWORD = "changePassword";

  public static final String ERROR_ROLE_ID_NULL = join(ERROR, ROLE, ID, NULL);
  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
  public static final String ERROR_NOT_FOUND_WITH_ID = join(ERROR_NOT_FOUND, WITH, ID);
  public static final String ERROR_SAVING = join(ERROR, SAVING);
  public static final String ERROR_HOME_FACILITY_NON_EXISTENT =
      join(ERROR, HOME_FACILITY, NON_EXISTENT);
  public static final String ERROR_PROGRAM_WITHOUT_FACILITY =
      join(ERROR, PROGRAM, WITHOUT, FACILITY);
  public static final String ERROR_ASSIGNED_ROLE_RIGHTS_EMPTY =
      join(ERROR, ASSIGNED_ROLE, RIGHTS, EMPTY);
  public static final String ERROR_EXTERNAL_CHANGE_PASSWORD_FAILED =
      join(ERROR, EXTERNAL, CHANGE_PASSWORD, FAILED);
  public static final String ERROR_EXTERNAL_RESET_PASSWORD_FAILED =
      join(ERROR, EXTERNAL, RESET_PASSWORD, FAILED);
}
