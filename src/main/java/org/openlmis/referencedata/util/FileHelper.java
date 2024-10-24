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

package org.openlmis.referencedata.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lombok.Setter;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.messagekeys.CsvUploadMessageKeys;
import org.openlmis.referencedata.util.messagekeys.MessageKeys;
import org.openlmis.referencedata.validate.CsvHeaderValidator;
import org.openlmis.referencedata.web.csv.model.ModelClass;
import org.openlmis.referencedata.web.csv.parser.CsvBeanReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileHelper {

  @Autowired
  private CsvHeaderValidator validator;

  @Setter
  @Value("${zipMaxSize}")
  protected String zipMaxSize;

  /**
   * Reads CSV data from an input stream and maps it to a list of objects of the specified class.
   *
   * @param clazz the class representing the target type for mapping CSV data
   * @param csvStream the input stream containing the CSV data to be read
   * @return a list of objects populated from the CSV
   * @throws ValidationMessageException if an error occurs while reading the multipart file or
   *                                    parsing the zip archive
   */
  public <T> List<T> readCsv(Class<T> clazz, InputStream csvStream) {
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

  /**
   * Converts a multipart file containing a zip archive to a map of file names to input streams of
   * the corresponding file contents.
   *
   * @param multipartFile the multipart file containing the zip archive
   * @return a map of file names to input streams of the corresponding file contents
   * @throws ValidationMessageException if an error occurs while reading the multipart file or
   *                                    parsing the zip archive
   */
  public Map<String, InputStream> convertMultipartFileToZipFileMap(
          MultipartFile multipartFile) {
    byte[] buffer = new byte[1024];

    try (ZipInputStream zipInputStream = new ZipInputStream(multipartFile.getInputStream())) {
      Map<String, InputStream> zipFileMap = new HashMap<>();
      ZipEntry zipEntry;
      int bytesRead;

      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        // Read bytes from zip file and write it to output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }

        zipFileMap.put(zipEntry.getName(), new ByteArrayInputStream(outputStream.toByteArray()));
      }

      if (zipFileMap.isEmpty()) {
        throw new ValidationMessageException(MessageKeys.ERROR_IO, "Empty archive");
      }

      return zipFileMap;
    } catch (IOException e) {
      throw new ValidationMessageException(e, MessageKeys.ERROR_IO, e.getMessage());
    }
  }

  /**
   * Validates given multipartFile. Checks if file has .zip extension and
   * size does not exceed maximum size.
   *
   * @param multipartFile the multipart file containing the zip archive
   * @throws ValidationMessageException if any of check fails
   */
  public void validateMultipartFile(MultipartFile multipartFile) {
    hasExpectedExtension(multipartFile.getOriginalFilename(), ".zip");
    hasValidSize(multipartFile);
  }

  /**
   * Validates given CSV file. Checks if file has .csv
   *
   * @param fileName the name of CSV file
   * @throws ValidationMessageException if any of check fails
   */
  public void validateCsvFile(String fileName, List<String> expectedFileNames) {
    hasExpectedExtension(fileName, ".csv");
    hasExpectedName(fileName, expectedFileNames);
  }

  private void hasExpectedName(String fileName, List<String> expectedFileNames) {
    if (!expectedFileNames.contains(fileName)) {
      throw new ValidationMessageException(
              new Message(CsvUploadMessageKeys.ERROR_FILE_NAME_INVALID, fileName));
    }
  }

  private void hasExpectedExtension(String fileName, String expectedExtension) {
    String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
    if (!fileExtension.equals(expectedExtension)) {
      throw new ValidationMessageException(new Message(
              CsvUploadMessageKeys.ERROR_FILE_EXTENSION, fileName,
              fileExtension, expectedExtension));
    }
  }

  private void hasValidSize(MultipartFile file) {
    long fileSize = file.getSize();
    if (fileSize > Long.parseLong(zipMaxSize)) {
      throw new ValidationMessageException(new Message(
              CsvUploadMessageKeys.ERROR_FILE_TOO_LARGE, zipMaxSize, fileSize));
    }
  }

}

