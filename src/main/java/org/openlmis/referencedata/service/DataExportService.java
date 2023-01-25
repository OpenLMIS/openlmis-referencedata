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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.ExportableDataRepository;
import org.openlmis.referencedata.util.messagekeys.MessageKeys;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataExportService {

  private static final String FORMATTER_SERVICE_NAME_SUFFIX = "FormatterService";
  private static final String REPOSITORY_NAME_SUFFIX = "Repository";
  @Setter(AccessLevel.PRIVATE)
  private String format;

  @Autowired
  private BeanFactory beanFactory;

  /**
   * Return zip archive with files in specific format.
   *
   * @param format    format of the files
   * @param filenames files to be included in the zip
   * @return byte data in zip format
   */
  public byte[] exportData(String format, String filenames) {
    String[] files = filenames.split(",");
    setFormat(format);
    ByteArrayOutputStream outputStream = toZip(files);

    return outputStream.toByteArray();
  }

  private ByteArrayOutputStream toZip(String[] files) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (ZipOutputStream zip = new ZipOutputStream(baos)) {
      for (Map.Entry<String, ByteArrayOutputStream> file : generateFiles(files).entrySet()) {
        ZipEntry entry = new ZipEntry(file.getKey() + "." + format);
        entry.setSize(file.getValue().toByteArray().length);
        zip.putNextEntry(entry);
        zip.write(file.getValue().toByteArray());
      }

      zip.closeEntry();
    } catch (IOException ex) {
      throw new ValidationMessageException(ex, MessageKeys.ERROR_IO, ex.getMessage());
    }

    return baos;
  }

  private Map<String, ByteArrayOutputStream> generateFiles(String[] filenames) {
    Map<String, ByteArrayOutputStream> output = new HashMap<>();
    for (String file : filenames) {
      output.put(file, generateFile(file));
    }
    return output;
  }

  private <T> ByteArrayOutputStream generateFile(String filename) {
    DataFormatterService formatter;
    ExportableDataRepository<T> repository;
    ByteArrayOutputStream output;
    try {
      formatter = beanFactory.getBean(format + FORMATTER_SERVICE_NAME_SUFFIX,
              DataFormatterService.class);

      repository = beanFactory.getBean(filename
              + REPOSITORY_NAME_SUFFIX, ExportableDataRepository.class);

      List<T> data = repository.findAll();
      output = new ByteArrayOutputStream();

      formatter.process(output, data, repository.getType());
    } catch (IOException | BeansException ex) {
      throw new ValidationMessageException(ex, MessageKeys.ERROR_IO, ex.getMessage());
    }

    return output;
  }

}


