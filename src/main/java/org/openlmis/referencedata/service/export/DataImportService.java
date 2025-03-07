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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openlmis.referencedata.dto.BaseDto;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.CsvUploadMessageKeys;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DataImportService {
  private static final List<String> IMPORT_ORDER =
      Arrays.asList(
          FacilityImportPersister.FACILITY_FILE_NAME,
          SupportedProgramImportPersister.SUPPORTED_PROGRAM_FILE_NAME,
          OrderableImportPersister.ORDERABLE_FILE_NAME,
          ProgramOrderableImportPersister.PROGRAM_ORDERABLE_FILE_NAME,
          TradeItemImportPersister.TRADE_ITEM_FILE_NAME,
          GeographicZonesImportPersister.GEOGRAPHIC_ZONE_FILE_NAME,
          UserImportPersister.USER_FILE_NAME
      );

  @Autowired private FileHelper fileHelper;
  @Autowired private BeanFactory beanFactory;

  /**
   * Imports the data from a ZIP with CSV files.
   *
   * @param zipFile ZIP archive being imported.
   * @throws InterruptedException when it was interrupted
   */
  @Transactional
  public List<ImportResponseDto.ImportDetails> importData(MultipartFile zipFile, Profiler profiler)
      throws InterruptedException {
    profiler.start("VALIDATE_ZIP_FILE");
    fileHelper.validateMultipartFile(zipFile);

    profiler.start("CONVERT_TO_ZIP_FILE_MAP");
    final Map<String, InputStream> fileMap = fileHelper.convertMultipartFileToZipFileMap(zipFile);

    profiler.start("VALIDATE_CSV_FILES");
    for (String fileName : fileMap.keySet()) {
      fileHelper.validateCsvFile(fileName, IMPORT_ORDER);
    }

    final List<ImportResponseDto.ImportDetails> result = new ArrayList<>();
    for (String importFileName : IMPORT_ORDER) {
      final InputStream fileStream = fileMap.get(importFileName);

      if (fileStream == null) {
        continue;
      }

      try {
        final Profiler entryProfiler = profiler.startNested("IMPORT_ZIP_ENTRY: " + importFileName);
        final DataImportPersister<?, ?, ? extends BaseDto> persister =
            beanFactory.getBean(importFileName, DataImportPersister.class);
        result.add(persister.processAndPersist(fileStream, entryProfiler));
      } catch (NoSuchBeanDefinitionException e) {
        throw new ValidationMessageException(
            e, new Message(CsvUploadMessageKeys.ERROR_FILE_NAME_INVALID, importFileName));
      }
    }

    return result;
  }
}
