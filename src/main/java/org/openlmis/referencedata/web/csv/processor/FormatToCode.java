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

import static org.openlmis.referencedata.util.messagekeys.CsvUploadMessageKeys.ERROR_UPLOAD_FORMATTING_FAILED;

import java.util.function.Function;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.Message;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.util.CsvContext;

public class FormatToCode<T> extends CellProcessorAdaptor implements StringCellProcessor {
  private final String typeName;
  private final Function<T, String> codeGetter;

  public FormatToCode(String typeName, Function<T, String> codeGetter) {
    this.typeName = typeName;
    this.codeGetter = codeGetter;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object execute(Object value, CsvContext context) {
    validateInputNotNull(value, context);

    try {
      final String code = codeGetter.apply((T) value);
      return next.execute(code, context);
    } catch (Exception e) {
      throw new ValidationMessageException(
          e,
          new Message(
              ERROR_UPLOAD_FORMATTING_FAILED,
              typeName + " code",
              value,
              context.getColumnNumber(),
              context.getRowNumber()));
    }
  }
}
