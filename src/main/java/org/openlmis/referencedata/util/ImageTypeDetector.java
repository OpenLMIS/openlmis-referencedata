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

import java.util.Optional;

public final class ImageTypeDetector {
  public static final String PNG = "image/png";
  public static final String JPEG = "image/jpeg";
  public static final String WEBP = "image/webp";

  private static final int[] PNG_SIGNATURE = {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
  private static final int[] JPEG_SIGNATURE = {0xFF, 0xD8, 0xFF};
  private static final int[] RIFF_SIGNATURE = {'R', 'I', 'F', 'F'};
  private static final int[] WEBP_SIGNATURE = {'W', 'E', 'B', 'P'};
  private static final int WEBP_SIGNATURE_OFFSET = 8;

  private ImageTypeDetector() {
    throw new UnsupportedOperationException();
  }

  /** Returns the content type of a PNG, JPEG or WebP image, and empty for anything else. */
  public static Optional<String> detect(byte[] data) {
    if (data == null) {
      return Optional.empty();
    }
    if (startsWith(data, 0, PNG_SIGNATURE)) {
      return Optional.of(PNG);
    }
    if (startsWith(data, 0, JPEG_SIGNATURE)) {
      return Optional.of(JPEG);
    }
    if (startsWith(data, 0, RIFF_SIGNATURE)
        && startsWith(data, WEBP_SIGNATURE_OFFSET, WEBP_SIGNATURE)) {
      return Optional.of(WEBP);
    }
    return Optional.empty();
  }

  private static boolean startsWith(byte[] data, int offset, int[] signature) {
    if (data.length < offset + signature.length) {
      return false;
    }
    for (int i = 0; i < signature.length; i++) {
      if ((data[offset + i] & 0xFF) != signature[i]) {
        return false;
      }
    }
    return true;
  }
}
