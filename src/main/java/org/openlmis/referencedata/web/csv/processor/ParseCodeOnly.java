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

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.Message;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.util.CsvContext;

public class ParseCodeOnly<T> extends CellProcessorAdaptor implements StringCellProcessor {
  private final String typeName;
  private final Supplier<T> instanceProvider;
  private final BiConsumer<T, String> codeSetter;

  /**
   * Create new instance of ParseCodeOnly cell processor.
   *
   * @param typeName the name of type to parse, used in exception texts, not null
   * @param instanceProvider the provider of new instance of an object to parse, not null
   * @param codeSetter the code setter in the new instance's type, not null
   */
  public ParseCodeOnly(
      String typeName, Supplier<T> instanceProvider, BiConsumer<T, String> codeSetter) {
    this.typeName = typeName;
    this.instanceProvider = instanceProvider;
    this.codeSetter = codeSetter;
  }

  @Override
  public T execute(Object value, CsvContext context) {
    validateInputNotNull(value, context);

    if (!(value instanceof String)) {
      throw getParseException(value, context);
    }

    T result = instanceProvider.get();
    codeSetter.accept(result, (String) value);
    return next.execute(result, context);
  }

  private ValidationMessageException getParseException(Object value, CsvContext context) {
    return new ValidationMessageException(
        new Message(
            ERROR_UPLOAD_PARSING_FAILED,
            value,
            typeName + " code",
            context.getColumnNumber(),
            context.getRowNumber()));
  }
}
