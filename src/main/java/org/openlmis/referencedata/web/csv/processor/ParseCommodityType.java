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

import static org.openlmis.referencedata.util.messagekeys.CsvUploadMessageKeys.ERROR_UPLOAD_PARSING_FAILED;

import java.util.regex.Pattern;
import org.openlmis.referencedata.dto.CommodityTypeDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.Message;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.util.CsvContext;

public class ParseCommodityType extends CellProcessorAdaptor implements StringCellProcessor {

  public static String SEPARATOR;

  @Override
  public Object execute(Object value, CsvContext context) {
    validateInputNotNull(value, context);

    CommodityTypeDto result;
    if (value instanceof String) {
      String[] parts = String.valueOf(value).split(Pattern.quote(SEPARATOR));

      if (parts.length != 2) {
        throw getParseException(value, context);
      }

      result = new CommodityTypeDto();
      result.setClassificationSystem(parts[0].trim());
      result.setClassificationId(parts[1].trim());
    } else {
      throw getParseException(value, context);
    }

    return next.execute(result, context);
  }

  private ValidationMessageException getParseException(Object value,
                                                       CsvContext context) {
    return new ValidationMessageException(
        new Message(ERROR_UPLOAD_PARSING_FAILED, value, "Commodity Type",
            context.getColumnNumber(), context.getRowNumber()));
  }

}
