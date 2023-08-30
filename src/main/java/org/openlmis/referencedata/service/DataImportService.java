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
import org.openlmis.referencedata.dto.BaseDto;
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

  public static final String ORDERABLE_CSV = "orderable.csv";

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
  public List<BaseDto> importData(MultipartFile zipFile) {
    List<BaseDto> result = new ArrayList<>();
    Map<String, InputStream> fileMap = FileHelper.convertMultipartFileToZipFileMap(zipFile);

    for (Map.Entry<String, InputStream> entry: fileMap.entrySet()) {
      if (entry.getKey().equals(ORDERABLE_CSV)) {
        result.addAll(processAndPersistOrderables(entry.getValue()));
      }
    }

    return result;
  }

  private List<OrderableDto> processAndPersistOrderables(InputStream dataStream) {
    List<OrderableDto> importedDtos = readCsv(OrderableDto.class, dataStream);
    List<Orderable> persistedObjects = orderableRepository.saveAll(
        createOrUpdateOrderables(importedDtos));

    return OrderableDto.newInstance(persistedObjects);
  }

  private List<Orderable> createOrUpdateOrderables(List<OrderableDto> dtoList) {
    List<Orderable> persistList = new ArrayList<>();

    for (OrderableDto orderableDto: dtoList) {
      Orderable latestOrderable = orderableRepository
          .findFirstByProductCodeOrderByIdentityVersionNumberDesc(
              Code.code(orderableDto.getProductCode()));

      persistList.add(orderableBuilder.newOrderable(orderableDto, latestOrderable));
    }

    return persistList;
  }

  private <T extends Identifiable> List<T> readCsv(Class<T> clazz, InputStream csvStream) {
    List<T> dtoList = new ArrayList<>();

    try {
      ModelClass<T> model = new ModelClass<>(clazz);
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
