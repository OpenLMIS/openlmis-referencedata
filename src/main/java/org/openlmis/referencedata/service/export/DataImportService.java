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
import java.util.List;
import java.util.Map;
import org.openlmis.referencedata.dto.BaseDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.CsvUploadMessageKeys;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DataImportService {

  @Autowired
  private FileHelper fileHelper;

  @Autowired
  private BeanFactory beanFactory;

  /**
   * Imports the data from a ZIP with CSV files.
   *
   * @param zipFile ZIP archive being imported.
   */
  @Transactional
  public List<BaseDto> importData(MultipartFile zipFile) {
    fileHelper.validateMultipartFile(zipFile);
    Map<String, InputStream> fileMap = fileHelper.convertMultipartFileToZipFileMap(zipFile);

    List<BaseDto> result = new ArrayList<>();
    for (Map.Entry<String, InputStream> entry: fileMap.entrySet()) {
      try {
        fileHelper.validateCsvFile(entry.getKey());
        DataImportPersister<?, ?, ? extends BaseDto> persister =
                beanFactory.getBean(entry.getKey(), DataImportPersister.class);
        result.addAll(persister.processAndPersist(entry.getValue()));
      } catch (NoSuchBeanDefinitionException e) {
        throw new ValidationMessageException(e, new Message(
                CsvUploadMessageKeys.ERROR_FILE_NAME_INVALID, entry.getKey()));
      }
    }

    return result;
  }

}
