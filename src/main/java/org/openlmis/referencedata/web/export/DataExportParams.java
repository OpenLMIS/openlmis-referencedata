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

package org.openlmis.referencedata.web.export;

import static org.openlmis.referencedata.util.messagekeys.DataExportMessageKeys.ERROR_LACK_PARAMS;
import static org.openlmis.referencedata.util.messagekeys.DataExportMessageKeys.ERROR_MISSING_DATA_PARAMETER;
import static org.openlmis.referencedata.util.messagekeys.DataExportMessageKeys.ERROR_MISSING_FORMAT_PARAMETER;

import java.util.Map;
import lombok.ToString;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.service.export.DataExportService;
import org.openlmis.referencedata.util.Message;

@ToString
public final class DataExportParams implements DataExportService.ExportParams {

  public static final String FORMAT = "format";
  public static final String DATA = "data";

  private final Map<String, String> queryParams;

  public DataExportParams(Map<String, String> queryParams) {
    this.queryParams = queryParams;
    validate();
  }

  @Override
  public String getFormat() {
    return queryParams.get(FORMAT);
  }

  @Override
  public String getData() {
    return queryParams.get(DATA);
  }

  /**
   * Checks if all params are present. Throws an exception if any parameter is missing.
   */
  private void validate() {
    if (queryParams.isEmpty()) {
      throw new ValidationMessageException(new Message(ERROR_LACK_PARAMS));
    } else if (!queryParams.containsKey(DATA)) {
      throw new ValidationMessageException(new Message(ERROR_MISSING_DATA_PARAMETER));
    } else if (!queryParams.containsKey(FORMAT)) {
      throw new ValidationMessageException(new Message(ERROR_MISSING_FORMAT_PARAMETER));
    }
  }
}
