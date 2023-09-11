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

import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.dto.TradeItemCsvModel;
import org.openlmis.referencedata.repository.TradeItemRepository;

@RunWith(MockitoJUnitRunner.class)
public class TradeItemServiceTest {

  @Mock
  private TradeItemRepository tradeItemRepository;

  @InjectMocks
  private TradeItemService service;

  @Test
  public void shouldReturnAllTradeItemCsvModels() {
    TradeItemCsvModel model1 = new TradeItemCsvModel("product-code-1", "manufacturer-1");
    TradeItemCsvModel model2 = new TradeItemCsvModel("product-code-2", "manufacturer-2");
    List<TradeItemCsvModel> modelList = Lists.newArrayList(model1, model2);
    final int modelListSize = modelList.size();
    when(tradeItemRepository.findAllTradeItemCsvModels()).thenReturn(modelList);

    List<TradeItemCsvModel> result = service.findAllExportableItems();

    verify(tradeItemRepository).findAllTradeItemCsvModels();
    assertEquals(result.size(), modelListSize);
  }

  @Test
  public void shouldReturnTradeItemCsvModelType() {
    TradeItemCsvModel model = new TradeItemCsvModel();

    Class<?> resultType = service.getExportableType();

    assertEquals(model.getClass(), resultType);
  }

}