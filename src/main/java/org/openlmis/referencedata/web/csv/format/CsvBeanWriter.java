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

package org.openlmis.referencedata.web.csv.format;

import lombok.Getter;
import org.openlmis.referencedata.dto.BaseDto;
import org.openlmis.referencedata.web.csv.model.ModelClass;
import org.openlmis.referencedata.web.csv.model.ModelField;
import org.openlmis.referencedata.web.csv.processor.CsvCellProcessors;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * This class has responsibility to instantiate a csvDozerBeanWriter from given inputStream.
 */
class CsvBeanWriter {

  private ModelClass modelClass;
  private CsvDozerBeanWriter csvDozerBeanWriter;
  private CellProcessor[] processors;

  @Getter
  private String[] headers;

  CsvBeanWriter(ModelClass modelClass,
                OutputStream outputStream) throws IOException {
    this.modelClass = modelClass;
    configureDozerBeanWriter(outputStream);
    configureProcessors();
  }

  void writeWithCellProcessors(List<? extends BaseDto> dtos) throws IOException {
    csvDozerBeanWriter.writeHeader(headers);
    for (Object dto : dtos) {
      csvDozerBeanWriter.write(dto, processors);
    }
    csvDozerBeanWriter.close();
  }

  private void configureDozerBeanWriter(OutputStream outputStream) throws IOException {
    CsvPreference csvPreference = new CsvPreference.Builder(CsvPreference.STANDARD_PREFERENCE)
        .surroundingSpacesNeedQuotes(true)
        .build();

    BufferedWriter bufferedReader = new BufferedWriter(
        new OutputStreamWriter(outputStream, "UTF-8"));
    csvDozerBeanWriter = new CsvDozerBeanWriter(bufferedReader, csvPreference);
    headers = readHeaders();
    String[] mappings = modelClass.getFieldNameMappings(headers);
    csvDozerBeanWriter.configureBeanMapping(modelClass.getClazz(), mappings);
  }

  private String[] readHeaders() {
    return modelClass.getImportFields().stream()
        .map(ModelField::getName)
        .toArray(String[]::new);
  }

  private void configureProcessors() {
    List<CellProcessor> cellProcessors =
        CsvCellProcessors.getProcessors(modelClass, asList(headers));
    processors = cellProcessors.toArray(new CellProcessor[cellProcessors.size()]);
  }
}
