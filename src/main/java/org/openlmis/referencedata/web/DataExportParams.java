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

package org.openlmis.referencedata.web;

import static java.util.Arrays.asList;
import static org.openlmis.referencedata.util.messagekeys.DataExportMessageKeys.ERROR_INVALID_PARAMS;

import java.util.List;
import java.util.Map;
import lombok.ToString;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.service.DataExportService;
import org.openlmis.referencedata.util.Message;

@ToString
public final class DataExportParams implements DataExportService.ExportParams {

  public static final String FORMAT = "format";
  public static final String DATA = "data";

  private static final List<String> ALL_PARAMETERS = asList(FORMAT, DATA);

  private final Map<String, String> queryParams;

  public DataExportParams(Map<String, String> queryParams) {
    this.queryParams = queryParams;
    validate();
  }

  @Override
  public String getFormat() {
    if (!queryParams.containsKey(FORMAT)) {
      return null;
    }
    return queryParams.get(FORMAT);
  }

  @Override
  public String getData() {
    if (!queryParams.containsKey(DATA)) {
      return null;
    }
    return queryParams.get(DATA);
  }

  /**
   * Checks if query params are valid. Throws an exception if any provided param is not on
   * supported list.
   */
  private void validate() {
    if (!ALL_PARAMETERS.containsAll(queryParams.keySet())) {
      throw new ValidationMessageException(new Message(ERROR_INVALID_PARAMS));
    }
  }
}
