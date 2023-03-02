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

public abstract class DataExportMessageKeys extends MessageKeys {

  private static final String ERROR = join(SERVICE_ERROR, DATA_EXPORT);

  private static final String ERROR_MISSING = join(ERROR, MISSING);

  public static final String ERROR_MISSING_FORMAT_PARAMETER =
          join(ERROR_MISSING, FORMAT, PARAMETER);

  public static final String ERROR_MISSING_DATA_PARAMETER =
          join(ERROR_MISSING, DATA, PARAMETER);

  public static final String ERROR_LACK_PARAMS = join(ERROR, LACKS_PARAMETERS);

}
