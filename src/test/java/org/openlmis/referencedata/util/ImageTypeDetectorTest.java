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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.Test;

public class ImageTypeDetectorTest {
  static final byte[] PNG = {
      (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D
  };
  static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10};
  static final byte[] WEBP = {
      'R', 'I', 'F', 'F', 0x24, 0x00, 0x00, 0x00, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' '
  };

  @Test
  public void shouldDetectPng() {
    assertEquals(Optional.of("image/png"), ImageTypeDetector.detect(PNG));
  }

  @Test
  public void shouldDetectJpeg() {
    assertEquals(Optional.of("image/jpeg"), ImageTypeDetector.detect(JPEG));
  }

  @Test
  public void shouldDetectWebp() {
    assertEquals(Optional.of("image/webp"), ImageTypeDetector.detect(WEBP));
  }

  @Test
  public void shouldRejectSvg() {
    byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>"
        .getBytes(StandardCharsets.UTF_8);

    assertFalse(ImageTypeDetector.detect(svg).isPresent());
  }

  @Test
  public void shouldRejectGif() {
    byte[] gif = "GIF89a".getBytes(StandardCharsets.US_ASCII);

    assertFalse(ImageTypeDetector.detect(gif).isPresent());
  }

  @Test
  public void shouldRejectRiffThatIsNotWebp() {
    byte[] wav = {'R', 'I', 'F', 'F', 0x24, 0x00, 0x00, 0x00, 'W', 'A', 'V', 'E'};

    assertFalse(ImageTypeDetector.detect(wav).isPresent());
  }

  @Test
  public void shouldRejectTooShortOrEmptyInput() {
    assertFalse(ImageTypeDetector.detect(new byte[]{(byte) 0x89, 0x50}).isPresent());
    assertFalse(ImageTypeDetector.detect(new byte[0]).isPresent());
    assertFalse(ImageTypeDetector.detect(null).isPresent());
  }
}
