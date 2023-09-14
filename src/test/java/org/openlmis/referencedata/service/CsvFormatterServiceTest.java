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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.service.export.CsvFormatterService;
import org.openlmis.referencedata.web.csv.format.CsvFormatter;
import org.openlmis.referencedata.web.csv.model.ModelClass;

@RunWith(MockitoJUnitRunner.class)
public class CsvFormatterServiceTest {

  @Mock
  private CsvFormatter csvFormatter;

  @InjectMocks
  private CsvFormatterService service;

  @Test
  public void shouldCallFormatterProcessMethod() throws IOException {
    // given
    OutputStream outputStream = mock(OutputStream.class);
    List data = mock(List.class);
    Class type = mock(Object.class).getClass();
    ModelClass modelClass = new ModelClass<>(type);

    // when
    service.process(outputStream, data, type);

    // then
    verify(csvFormatter).process(outputStream, modelClass, data);
  }

}