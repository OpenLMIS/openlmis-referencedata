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

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.service.DataExportService.FORMATTER_SERVICE_NAME_SUFFIX;
import static org.openlmis.referencedata.service.DataExportService.SERVICE_NAME_SUFFIX;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.web.DataExportParams;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@RunWith(MockitoJUnitRunner.class)
public class DataExportServiceTest {

  private Map<String, String> queryParamsMap;

  private final InputStream inputStream = new ByteArrayInputStream("test-input-data".getBytes());

  @Mock
  private BeanFactory beanFactory;

  @Mock
  private ResourceLoader loader;

  @Mock
  private CsvFormatterService csvFormatterService;

  @Mock
  private OrderableService orderableService;

  @Mock
  private Resource resource;

  @InjectMocks
  private DataExportService dataExportService;

  @Before
  public void setUp() {
    queryParamsMap = new HashMap<String, String>() {
      {
        put("data", "data-value");
        put("format", "format-value");
      }
    };
  }

  @Test
  public void shouldReturnArrayOfBytes() throws IOException {
    List<OrderableDto> productList = mock(List.class);

    setPreconditionsForServices();
    when(orderableService.findAll()).thenReturn(productList);
    when(orderableService.getType()).thenReturn(OrderableDto.class);
    when(loader.getResource(anyString())).thenReturn(resource);
    when(resource.getInputStream()).thenReturn(inputStream);

    byte[] result = dataExportService.exportData(new DataExportParams(queryParamsMap));

    assertThat(result, is(instanceOf(byte[].class)));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfWrongFormatProvided() {
    final String invalidFormat = "invalid-format-value";
    queryParamsMap.replace("format", invalidFormat);

    when(beanFactory.getBean(invalidFormat + FORMATTER_SERVICE_NAME_SUFFIX,
            DataFormatterService.class)).thenThrow(mock(BeansException.class));

    dataExportService.exportData(new DataExportParams(queryParamsMap));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfWrongDataProvided() {
    final String invalidData = "invalid-data-value";
    queryParamsMap.replace("data", invalidData);

    when(beanFactory.getBean(invalidData + SERVICE_NAME_SUFFIX,
            ExportableDataService.class)).thenThrow(mock(BeansException.class));

    dataExportService.exportData(new DataExportParams(queryParamsMap));
  }

  @Test
  public void shouldReturnArrayOfBytesIfNoDataFound() throws IOException {
    List<OrderableDto> emptyProductList = Lists.emptyList();

    setPreconditionsForServices();
    when(orderableService.findAll()).thenReturn(emptyProductList);
    when(loader.getResource(anyString())).thenReturn(resource);
    when(resource.getInputStream()).thenReturn(inputStream);

    byte[] result = dataExportService.exportData(new DataExportParams(queryParamsMap));

    assertThat(result, is(notNullValue()));
    assertThat(result, is(instanceOf(byte[].class)));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldReturnExceptionIfNoMappingFileFound() throws IOException {
    setPreconditionsForServices();
    when(loader.getResource(anyString())).thenReturn(resource);
    when(resource.getInputStream()).thenThrow(IOException.class);

    dataExportService.exportData(new DataExportParams(queryParamsMap));
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldReturnExceptionIfFilenameContainsParentDirectoryIndicators() {
    final String dataWithParentDirIndicator = "../../../data-value";
    queryParamsMap.replace("data", dataWithParentDirIndicator);
    setPreconditionsForServices();

    dataExportService.exportData(new DataExportParams(queryParamsMap));
  }

  private void setPreconditionsForServices() {
    doAnswer(invocation -> orderableService).when(beanFactory).getBean(anyString(),
            eq(ExportableDataService.class));
    doAnswer(invocation -> csvFormatterService).when(beanFactory).getBean(anyString(),
            eq(DataFormatterService.class));
  }

}