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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.messagekeys.MessageKeys;
import org.springframework.web.multipart.MultipartFile;

public final class FileHelper {

  private FileHelper() {
    throw new UnsupportedOperationException();
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
  public static Map<String, InputStream> convertMultipartFileToZipFileMap(
          MultipartFile multipartFile) {
    try (ZipInputStream zipInputStream = new ZipInputStream(multipartFile.getInputStream())) {
      Map<String, InputStream> zipFileMap = new HashMap<>();
      ZipEntry zipEntry;
      if ((zipEntry = zipInputStream.getNextEntry()) == null) {
        throw new ValidationMessageException(MessageKeys.ERROR_IO);
      } else {
        zipFileMap.put(zipEntry.getName(), zipInputStream);
      }
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        zipFileMap.put(zipEntry.getName(), zipInputStream);
      }

      return zipFileMap;
    } catch (IOException e) {
      throw new ValidationMessageException(e, MessageKeys.ERROR_IO, e.getMessage());
    }
  }

}

