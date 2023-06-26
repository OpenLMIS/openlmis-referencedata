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

import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.dto.DispensableDto;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

public class FormatDispensable extends CellProcessorAdaptor implements StringCellProcessor {

  public static final String DISPENSABLE_ATTRIBUTE_DISPLAY_FORMAT = "%s:%s";

  @Override
  public Object execute(Object value, CsvContext context) {
    validateInputNotNull(value, context);

    String result;
    if (value instanceof DispensableDto) {
      DispensableDto dispensable = (DispensableDto) value;

      if (dispensable.getAttributes().get(Dispensable.KEY_SIZE_CODE) != null) {
        result = String.format(DISPENSABLE_ATTRIBUTE_DISPLAY_FORMAT, Dispensable.KEY_SIZE_CODE,
                dispensable.getAttributes().get(Dispensable.KEY_SIZE_CODE));
      } else if (dispensable.getAttributes().get(Dispensable.KEY_DISPENSING_UNIT) != null) {
        result = String.format(DISPENSABLE_ATTRIBUTE_DISPLAY_FORMAT,
                Dispensable.KEY_DISPENSING_UNIT,
                dispensable.getAttributes().get(Dispensable.KEY_DISPENSING_UNIT));
      } else {
        throw getSuperCsvCellProcessorException(dispensable, context);
      }

    } else {
      throw getSuperCsvCellProcessorException(value, context);
    }

    return next.execute(result, context);
  }

  private SuperCsvCellProcessorException getSuperCsvCellProcessorException(Object value,
                                                                           CsvContext context) {
    return new SuperCsvCellProcessorException(
            String.format("Cannot get dispensing unit or size code from '%s'.", value.toString()),
            context, this);
  }

}
