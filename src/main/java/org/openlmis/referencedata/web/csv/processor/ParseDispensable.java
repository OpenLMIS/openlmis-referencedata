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

import java.util.HashMap;
import java.util.Map;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.dto.DispensableDto;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

public class ParseDispensable extends CellProcessorAdaptor implements StringCellProcessor {

  @Override
  public <T> T execute(Object value, CsvContext context) {
    validateInputNotNull(value, context);

    Dispensable result;
    if (value instanceof String) {
      String[] parts = ((String) value).split(":", 2);
      if (parts.length != 2) {
        throw getSuperCsvCellProcessorException(value, context, null);
      }

      DispensableDto dto = new DispensableDto();
      Map<String, String> attributes = new HashMap<>();
      attributes.put(parts[0], parts[1]);
      dto.setAttributes(attributes);

      result = Dispensable.createNew(dto);
    } else {
      throw getSuperCsvCellProcessorException(value, context, null);
    }

    return next.execute(result, context);
  }

  private SuperCsvCellProcessorException getSuperCsvCellProcessorException(Object value,
                                                                           CsvContext context,
                                                                           Exception cause) {
    return new SuperCsvCellProcessorException(String.format(
            "'%s' could not be parsed to Dispensable", value), context, this, cause);
  }
}
