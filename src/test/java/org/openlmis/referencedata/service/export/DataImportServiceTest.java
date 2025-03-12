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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.util.FileHelper;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.multipart.MultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class DataImportServiceTest {

  @Rule public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
  private Map<String, InputStream> fileMap;
  private DataImportPersister<?, ?, ?> dataImportPersister;
  @Mock private FileHelper fileHelper;

  @Mock private BeanFactory beanFactory;

  @Mock private Profiler profiler;

  @InjectMocks private DataImportService dataImportService;

  @Before
  public void setUp() {
    fileMap = new HashMap<>();
    fileMap.put("facility.csv", mock(InputStream.class));
    dataImportPersister = mock(DataImportPersister.class);

    when(profiler.startNested(anyString())).thenReturn(profiler);
  }

  @Test
  public void shouldSuccessfullyImportData() throws InterruptedException {
    // Given
    when(fileHelper.convertMultipartFileToZipFileMap(any(MultipartFile.class))).thenReturn(fileMap);
    when(beanFactory.getBean(anyString(), eq(DataImportPersister.class)))
        .thenReturn(dataImportPersister);
    when(dataImportPersister.processAndPersist(any(InputStream.class), any(Profiler.class)))
        .thenReturn(mock(ImportResponseDto.ImportDetails.class));

    // When
    List<ImportResponseDto.ImportDetails> result =
        dataImportService.importData(mock(MultipartFile.class), profiler);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
  }
}
