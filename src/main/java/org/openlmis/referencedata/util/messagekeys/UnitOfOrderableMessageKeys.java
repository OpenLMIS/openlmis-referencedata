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

public class UnitOfOrderableMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, UNIT_OF_ORDERABLE);

  public static final String DISPLAY_ORDER = "displayOrder";
  public static final String FACTOR = "factor";
  public static final String NAME = "name";

  public static final String ERROR_NOT_FOUND = join(ERROR, NOT_FOUND);
  public static final String ERROR_NULL = join(ERROR, NULL);
  public static final String ERROR_MUST_BE_POSITIVE_OR_ZERO = join(ERROR, MUST_BE_POSITIVE_OR_ZERO);

  public static final String ERROR_NAME_REQUIRED = join(ERROR, NAME, REQUIRED);
  public static final String ERROR_DISPLAY_ORDER_REQUIRED =
      join(ERROR, DISPLAY_ORDER, REQUIRED);
  public static final String ERROR_FACTOR_REQUIRED = join(ERROR, FACTOR, REQUIRED);
  public static final String ERROR_DISPLAY_ORDER_MUST_BE_POSITIVE_OR_ZERO =
      join(ERROR, DISPLAY_ORDER, MUST_BE_POSITIVE_OR_ZERO);
  public static final String ERROR_FACTOR_MUST_BE_POSITIVE_OR_ZERO =
      join(ERROR, FACTOR, MUST_BE_POSITIVE_OR_ZERO);
}
