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
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Identifiable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.OrderableBuilder;
import org.openlmis.referencedata.util.messagekeys.MessageKeys;
import org.openlmis.referencedata.validate.CsvHeaderValidator;
import org.openlmis.referencedata.web.csv.model.ModelClass;
import org.openlmis.referencedata.web.csv.parser.CsvBeanReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DataImportService {

  private static final String ORDERABLE_CSV = "orderable.csv";

  @Autowired
  private CsvHeaderValidator validator;

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private OrderableBuilder orderableBuilder;

  /**
   * Imports the data from a ZIP with CSV files.
   *
   * @param zipFile ZIP archive being imported.
   */
  @Transactional
  public List<?> importData(MultipartFile zipFile) {
    List<Object> result = new ArrayList<>();
    Map<String, InputStream> fileMap = FileHelper.convertMultipartFileToZipFileMap(zipFile);

    for (Map.Entry<String, InputStream> entry: fileMap.entrySet()) {
      if (entry.getKey().equals(ORDERABLE_CSV)) {
        List<Orderable> persistList = new ArrayList<>();
        List<OrderableDto> importedDtos = readCsv(OrderableDto.class, entry.getValue());

        for (OrderableDto orderableDto: importedDtos) {
          Orderable latestOrderable = orderableRepository
              .findFirstByProductCodeOrderByIdentityVersionNumberDesc(
                  Code.code(orderableDto.getProductCode()));

          if (latestOrderable == null) {
            persistList.add(orderableBuilder.newOrderable(orderableDto, null));
          } else {
            persistList.add(orderableBuilder.newOrderable(orderableDto, latestOrderable));
          }
        }

        result.addAll(OrderableDto.newInstance(
            orderableRepository.saveAll(persistList)));
      }
    }

    return result;
  }

  private <T extends Identifiable> List<T> readCsv(Class<T> clazz, InputStream csvStream) {
    ModelClass<T> model = new ModelClass<>(clazz);
    List<T> dtoList = new ArrayList<>();

    try {
      CsvBeanReader<T> reader = new CsvBeanReader<>(
          model,
          csvStream,
          validator);

      T readObject;
      while ((readObject = reader.readWithCellProcessors()) != null) {
        dtoList.add(readObject);
      }
    } catch (IOException e) {
      throw new ValidationMessageException(e, MessageKeys.ERROR_IO, e.getMessage());
    }

    return dtoList;
  }

}
