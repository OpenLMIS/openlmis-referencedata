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

public abstract class RequisitionGroupMessageKeys extends MessageKeys {
  private static final String REQUISITION_GROUP = "requisitionGroup";
  private static final String ERROR = join(SERVICE_ERROR, REQUISITION_GROUP);
  private static final String TOO_LONG = "tooLong";
  private static final String DESCRIPTION = "description";

  public static final String ERROR_NULL = join(ERROR, NULL);

  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);

  public static final String ERROR_DESCRIPTION_TOO_LONG = join(ERROR, DESCRIPTION, TOO_LONG);

  public static final String ERROR_CODE_REQUIRED = join(ERROR, CODE, REQUIRED);
  public static final String ERROR_CODE_DUPLICATED = join(ERROR, CODE, DUPLICATED);
  public static final String ERROR_CODE_TOO_LONG = join(ERROR, CODE, TOO_LONG);

  public static final String ERROR_NAME_TOO_LONG = join(ERROR, NAME, TOO_LONG);
  public static final String ERROR_NAME_REQUIRED = join(ERROR, NAME, REQUIRED);

  public static final String ERROR_FACILITY_NULL = join(ERROR, FACILITY, NULL);
  public static final String ERROR_FACILITY_ID_REQUIRED = join(ERROR, FACILITY, REQUIRED);
  public static final String ERROR_FACILITY_NON_EXISTENT = join(ERROR, FACILITY, NON_EXISTENT);

  public static final String ERROR_SUPERVISORY_NODE_REQUIRED =
      join(ERROR, SUPERVISORY_NODE, REQUIRED);
  public static final String ERROR_SUPERVISORY_NODE_ID_REQUIRED =
      join(ERROR, SUPERVISORY_NODE, REQUIRED);
  public static final String ERROR_SUPERVISORY_NODE_NON_EXISTENT =
      join(ERROR, SUPERVISORY_NODE, NON_EXISTENT);
}
