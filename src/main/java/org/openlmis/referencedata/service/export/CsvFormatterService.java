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

package org.openlmis.referencedata.service.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.openlmis.referencedata.web.csv.format.CsvFormatter;
import org.openlmis.referencedata.web.csv.model.ModelClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CsvFormatterService implements DataFormatterService {

  @Autowired
  private CsvFormatter csvFormatter;

  /**
   * Calls the process method of the {@link CsvFormatter} class that parses data into
   * specific model.
   *
   * @param outputStream output stream to which the data will be written
   * @param data         list of objects to be parsed
   * @param type         class type of objects
   * @param <T>          type of objects contained in data
   */
  @Override
  public <T> void process(OutputStream outputStream, List<T> data, Class<T> type)
          throws IOException {
    csvFormatter.process(outputStream, new ModelClass(type), data);
  }

}
