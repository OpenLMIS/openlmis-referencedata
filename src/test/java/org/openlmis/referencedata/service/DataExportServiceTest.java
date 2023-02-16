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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.web.DataExportParams;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;

@RunWith(MockitoJUnitRunner.class)
public class DataExportServiceTest {

  private static final String EACH = "each";

  private Map<String, String> queryParamsMap;

  @Mock
  private BeanFactory beanFactory;

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
    Orderable product1 = new Orderable(Code.code("ibuprofen"), Dispensable.createNew(EACH), 10, 4,
            false, UUID.randomUUID(), 1L);
    Orderable product2 = new Orderable(Code.code("paracetamol"), Dispensable.createNew(EACH), 10, 4,
            false, UUID.randomUUID(), 1L);
    List<Orderable> productList = Arrays.asList(product1, product2);
    CsvFormatterService csvFormatterService = mock(CsvFormatterService.class);
    OrderableService orderableService = mock(OrderableService.class);

    doAnswer(invocation -> orderableService).when(beanFactory).getBean(anyString(),
            eq(ExportableDataService.class));
    doAnswer(invocation -> csvFormatterService).when(beanFactory).getBean(anyString(),
            eq(DataFormatterService.class));
    when(orderableService.findAll()).thenReturn(productList);
    when(orderableService.getType()).thenReturn(Orderable.class);

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
  public void shouldReturnArrayOfBytesIfNoDataFound() {
    List<Orderable> emptyProductList = Lists.emptyList();
    CsvFormatterService csvFormatterService = mock(CsvFormatterService.class);
    OrderableService orderableService = mock(OrderableService.class);

    doAnswer(invocation -> orderableService).when(beanFactory).getBean(anyString(),
            eq(ExportableDataService.class));
    doAnswer(invocation -> csvFormatterService).when(beanFactory).getBean(anyString(),
            eq(DataFormatterService.class));
    when(orderableService.findAll()).thenReturn(emptyProductList);

    byte[] result = dataExportService.exportData(new DataExportParams(queryParamsMap));

    assertThat(result, is(notNullValue()));
    assertThat(result, is(instanceOf(byte[].class)));
  }

}