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

package org.openlmis.referencedata.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.messagekeys.MessageKeys;
import org.openlmis.referencedata.validate.CsvHeaderValidator;
import org.openlmis.referencedata.web.csv.model.ModelClass;
import org.openlmis.referencedata.web.csv.parser.CsvBeanReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DataImportService {

  private static final String ORDERABLE_CSV = "orderable.csv";

  @Autowired
  private CsvHeaderValidator validator;

  /**
   * Imports the data from a ZIP with CSV files.
   *
   * @param zipFile ZIP archive being imported.
   */
  public List<OrderableDto> importData(MultipartFile zipFile) {
    ModelClass<OrderableDto> model = new ModelClass<>(OrderableDto.class);
    List<OrderableDto> orderableDtoList = new ArrayList<>();

    Map<String, InputStream> fileMap = FileHelper.convertMultipartFileToZipFileMap(zipFile);
    InputStream csvStream = fileMap.get(ORDERABLE_CSV);

    try {
      CsvBeanReader<OrderableDto> reader = new CsvBeanReader<>(
          model,
          csvStream,
          validator);

      OrderableDto orderable;
      while ((orderable = reader.readWithCellProcessors()) != null) {
        orderableDtoList.add(orderable);
      }
    } catch (IOException e) {
      throw new ValidationMessageException(e, MessageKeys.ERROR_IO, e.getMessage());
    }

    return orderableDtoList;
  }

}
