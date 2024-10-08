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

package org.openlmis.referencedata.web.csv.processor;

import static org.openlmis.referencedata.util.messagekeys.CsvUploadMessageKeys.ERROR_UPLOAD_PARSING_LOCAL_DATE_FAILED;

import java.time.LocalDate;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.Message;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.util.CsvContext;

public class ParseLocalDate extends CellProcessorAdaptor implements StringCellProcessor {
  @Override
  public <T> T execute(Object value, CsvContext context) {
    validateInputNotNull(value, context);

    if (!(value instanceof String)) {
      throw new ValidationMessageException(
          new Message(
              ERROR_UPLOAD_PARSING_LOCAL_DATE_FAILED,
              context.getColumnNumber(),
              context.getRowNumber()));
    }

    try {
      return next.execute(LocalDate.parse((String) value), context);
    } catch (Exception e) {
      throw new ValidationMessageException(
          e,
          new Message(
              ERROR_UPLOAD_PARSING_LOCAL_DATE_FAILED,
              context.getColumnNumber(),
              context.getRowNumber()));
    }
  }
}
