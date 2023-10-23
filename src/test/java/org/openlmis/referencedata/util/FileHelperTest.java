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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class FileHelperTest {

  @InjectMocks
  private FileHelper fileHelper;

  @Rule
  public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Before
  public void setUp() {
    environmentVariables.set("zipMaxSize", "1000000");
  }

  @Test
  public void shouldConvertMultipartFileToZipFileMapWithValidZipFile() throws IOException {
    byte[] fileContent = createValidZipFileContent();
    MockMultipartFile mockMultipartFile = new MockMultipartFile("test.zip", fileContent);

    Map<String, InputStream> result = fileHelper
            .convertMultipartFileToZipFileMap(mockMultipartFile);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertNotNull(result.get("file1.txt"));
    assertNotNull(result.get("file2.txt"));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldConvertMultipartFileToZipFileMapWithInvalidZipFile() {
    String fileName = "test.zip";
    String fileContent = "This is not a valid zip file.";
    MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName,
            "application/zip", fileContent.getBytes());
    fileHelper.convertMultipartFileToZipFileMap(mockMultipartFile);
  }

  private byte[] createValidZipFileContent() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zipOutputStream = new ZipOutputStream(baos);

    ZipEntry file1Entry = new ZipEntry("file1.txt");
    zipOutputStream.putNextEntry(file1Entry);
    zipOutputStream.write("This is the contents of file1.txt".getBytes());
    zipOutputStream.closeEntry();

    ZipEntry file2Entry = new ZipEntry("file2.txt");
    zipOutputStream.putNextEntry(file2Entry);
    zipOutputStream.write("This is the contents of file2.txt".getBytes());
    zipOutputStream.closeEntry();

    zipOutputStream.close();
    return baos.toByteArray();
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenValidateCsvWithWrongExtension() {
    String fileName = "test.txt";
    fileHelper.validateCsvFile(fileName);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenValidateZipWithWrongExtension() {
    MultipartFile multipartFile = mock(MultipartFile.class);
    when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
    fileHelper.validateMultipartFile(multipartFile);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenZipIsTooLarge() {
    MultipartFile multipartFile = mock(MultipartFile.class);
    when(multipartFile.getOriginalFilename()).thenReturn("test.zip");
    when(multipartFile.getSize()).thenReturn(5000000L);
    fileHelper.validateMultipartFile(multipartFile);
  }

}
