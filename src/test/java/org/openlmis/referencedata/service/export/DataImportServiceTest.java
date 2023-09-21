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
import java.util.Collections;
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
import org.openlmis.referencedata.dto.BaseDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.FileHelper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.multipart.MultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class DataImportServiceTest {

  private Map<String, InputStream> fileMap;
  private DataImportPersister<?, ?, ?> dataImportPersister;

  @Rule
  public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Mock
  private FileHelper fileHelper;

  @Mock
  private BeanFactory beanFactory;

  @InjectMocks
  private DataImportService dataImportService;

  @Before
  public void setUp() {
    fileMap = new HashMap<>();
    fileMap.put("test.csv", mock(InputStream.class));
    dataImportPersister = mock(DataImportPersister.class);
  }

  @Test
  public void shouldSuccessfullyImportData() {
    // Given
    when(fileHelper.convertMultipartFileToZipFileMap(any(MultipartFile.class)))
        .thenReturn(fileMap);
    when(beanFactory.getBean(anyString(), eq(DataImportPersister.class))).thenReturn(
        dataImportPersister);
    when(dataImportPersister.processAndPersist(any(InputStream.class)))
        .thenReturn((List) Collections.singletonList(mock(BaseDto.class)));

    // When
    List<BaseDto> result = dataImportService.importData(mock(MultipartFile.class));

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowErrorIfBeanNotFound() {
    // given
    when(fileHelper.convertMultipartFileToZipFileMap(any(MultipartFile.class)))
        .thenReturn(fileMap);
    when(beanFactory.getBean(anyString(), eq(DataImportPersister.class)))
        .thenThrow(ValidationMessageException.class);

    // when
    dataImportService.importData(mock(MultipartFile.class));
  }

}