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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.TradeItemCsvModel;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.testbuilder.TradeItemDataBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class TradeItemServiceTest {

  private static final Pageable PAGEABLE = PageRequest.of(0, 10);
  private static final String GTIN_13 = "5901234123457";
  private static final String PADDED_GTIN_13 = "05901234123457";
  private static final String GTIN_14 = "05890123456786";
  private static final String NOT_A_GTIN = "not-a-gtin";
  private static final String PADDED_NOT_A_GTIN = "0000" + NOT_A_GTIN;

  @Mock
  private TradeItemRepository tradeItemRepository;

  @InjectMocks
  private TradeItemService service;

  @Test
  public void shouldReturnAllTradeItemCsvModels() {
    TradeItemCsvModel model1 = new TradeItemCsvModel("product-code-1", "manufacturer-1", null);
    TradeItemCsvModel model2 = new TradeItemCsvModel("product-code-2", "manufacturer-2", null);
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

  @Test
  public void shouldSearchTradeItemsByIds() {
    TradeItem tradeItem = new TradeItemDataBuilder().build();
    Set<UUID> ids = Collections.singleton(tradeItem.getId());
    TradeItemSearchParams params = new TradeItemSearchParams(ids, null, false, null);
    when(tradeItemRepository.findAllById(ids)).thenReturn(Lists.newArrayList(tradeItem));

    Page<TradeItem> result = service.search(params, PAGEABLE);

    verify(tradeItemRepository).findAllById(ids);
    assertEquals(1, result.getContent().size());
    assertEquals(tradeItem, result.getContent().get(0));
  }

  @Test
  public void shouldSearchTradeItemsByClassificationIdWithFullMatch() {
    TradeItem tradeItem = new TradeItemDataBuilder().build();
    String classificationId = "classification-1";
    TradeItemSearchParams params = new TradeItemSearchParams(null, classificationId, true, null);
    when(tradeItemRepository.findByClassificationId(classificationId))
        .thenReturn(Lists.newArrayList(tradeItem));

    Page<TradeItem> result = service.search(params, PAGEABLE);

    verify(tradeItemRepository).findByClassificationId(classificationId);
    assertEquals(1, result.getContent().size());
  }

  @Test
  public void shouldSearchTradeItemsByClassificationIdWithPartialMatch() {
    TradeItem tradeItem = new TradeItemDataBuilder().build();
    String classificationId = "classification";
    TradeItemSearchParams params = new TradeItemSearchParams(null, classificationId, false, null);
    when(tradeItemRepository.findByClassificationIdLike(classificationId))
        .thenReturn(Lists.newArrayList(tradeItem));

    Page<TradeItem> result = service.search(params, PAGEABLE);

    verify(tradeItemRepository).findByClassificationIdLike(classificationId);
    assertEquals(1, result.getContent().size());
  }

  @Test
  public void shouldSearchTradeItemsByGtin() {
    TradeItem tradeItem = new TradeItemDataBuilder().build();
    TradeItemSearchParams params = new TradeItemSearchParams(null, null, false, GTIN_14);
    when(tradeItemRepository.findByGtin(GTIN_14)).thenReturn(Optional.of(tradeItem));

    Page<TradeItem> result = service.search(params, PAGEABLE);

    verify(tradeItemRepository).findByGtin(GTIN_14);
    assertEquals(1, result.getContent().size());
    assertEquals(tradeItem, result.getContent().get(0));
  }

  @Test
  public void shouldNormalizeGtinBeforeSearching() {
    TradeItem tradeItem = new TradeItemDataBuilder().build();
    TradeItemSearchParams params = new TradeItemSearchParams(null, null, false, GTIN_13);
    when(tradeItemRepository.findByGtin(PADDED_GTIN_13)).thenReturn(Optional.of(tradeItem));

    Page<TradeItem> result = service.search(params, PAGEABLE);

    verify(tradeItemRepository).findByGtin(PADDED_GTIN_13);
    assertEquals(1, result.getContent().size());
  }

  @Test
  public void shouldReturnEmptyPageWhenNoTradeItemIsRegisteredWithGtin() {
    TradeItemSearchParams params = new TradeItemSearchParams(null, null, false, GTIN_14);
    when(tradeItemRepository.findByGtin(GTIN_14)).thenReturn(Optional.empty());

    Page<TradeItem> result = service.search(params, PAGEABLE);

    verify(tradeItemRepository).findByGtin(GTIN_14);
    assertEquals(0, result.getContent().size());
    assertEquals(0, result.getTotalElements());
  }

  @Test
  public void shouldReturnEmptyPageWhenGtinCannotMatchAnyTradeItem() {
    TradeItemSearchParams params = new TradeItemSearchParams(null, null, false, NOT_A_GTIN);
    when(tradeItemRepository.findByGtin(PADDED_NOT_A_GTIN)).thenReturn(Optional.empty());

    Page<TradeItem> result = service.search(params, PAGEABLE);

    verify(tradeItemRepository).findByGtin(PADDED_NOT_A_GTIN);
    assertEquals(0, result.getContent().size());
  }

  @Test
  public void shouldIgnoreOtherFiltersWhenGtinIsProvided() {
    TradeItem tradeItem = new TradeItemDataBuilder().build();
    TradeItemSearchParams params = new TradeItemSearchParams(
        Collections.singleton(UUID.randomUUID()), "classification-1", true, GTIN_14);
    when(tradeItemRepository.findByGtin(GTIN_14)).thenReturn(Optional.of(tradeItem));

    Page<TradeItem> result = service.search(params, PAGEABLE);

    assertEquals(1, result.getContent().size());
    verify(tradeItemRepository).findByGtin(GTIN_14);
    verify(tradeItemRepository, never()).findAllById(any());
    verify(tradeItemRepository, never()).findByClassificationId(anyString());
  }

  @Test
  public void shouldIgnoreBlankGtin() {
    TradeItem tradeItem = new TradeItemDataBuilder().build();
    TradeItemSearchParams params = new TradeItemSearchParams(null, null, false, " ");
    Page<TradeItem> page = new PageImpl<>(Lists.newArrayList(tradeItem));
    when(tradeItemRepository.findAll(PAGEABLE)).thenReturn(page);

    Page<TradeItem> result = service.search(params, PAGEABLE);

    verify(tradeItemRepository).findAll(PAGEABLE);
    verify(tradeItemRepository, never()).findByGtin(anyString());
    assertEquals(1, result.getContent().size());
  }

  @Test
  public void shouldReturnAllTradeItemsWhenNoParamsProvided() {
    TradeItem tradeItem = new TradeItemDataBuilder().build();
    TradeItemSearchParams params = new TradeItemSearchParams(null, null, false, null);
    Page<TradeItem> page = new PageImpl<>(Lists.newArrayList(tradeItem));
    when(tradeItemRepository.findAll(PAGEABLE)).thenReturn(page);

    Page<TradeItem> result = service.search(params, PAGEABLE);

    verify(tradeItemRepository).findAll(PAGEABLE);
    assertEquals(1, result.getContent().size());
  }

}
