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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.dto.OrderableIdentifierCsvModel;
import org.openlmis.referencedata.repository.OrderableRepository;

@RunWith(MockitoJUnitRunner.class)
public class OrderableIdentifierServiceTest {

  @Mock
  private OrderableRepository orderableRepository;

  @InjectMocks
  private OrderableIdentifierService service;

  @Test
  public void shouldReturnAllOrderableIdentifierCsvModels() {
    OrderableIdentifierCsvModel model1 =
            new OrderableIdentifierCsvModel("identifier-key-1", "product-code-1");
    OrderableIdentifierCsvModel model2 =
            new OrderableIdentifierCsvModel("identifier-key-2", "product-code-2");
    List<OrderableIdentifierCsvModel> modelList = Lists.newArrayList(model1, model2);
    final int modelListSize = modelList.size();
    when(orderableRepository.findAllOrderableIdentifierCsvModels()).thenReturn(modelList);

    List<OrderableIdentifierCsvModel> result = service.findAllExportableItems();

    verify(orderableRepository).findAllOrderableIdentifierCsvModels();
    assertEquals(result.size(), modelListSize);
  }

  @Test
  public void shouldReturnOrderableIdentifierCsvModelType() {
    OrderableIdentifierCsvModel model = new OrderableIdentifierCsvModel();

    Class<?> resultType = service.getExportableType();

    assertEquals(model.getClass(), resultType);
  }

}