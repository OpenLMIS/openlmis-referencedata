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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.LotRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.testbuilder.LotDataBuilder;
import org.openlmis.referencedata.testbuilder.TradeItemDataBuilder;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class LotServiceTest {

  @Mock
  private LotRepository lotRepository;

  @Mock
  private TradeItemRepository tradeItemRepository;

  @InjectMocks
  private LotService lotService = new LotService();

  private Pageable pageable;

  private TradeItem tradeItem;

  private Lot lot;

  private Page<Lot> expected;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    pageable = new PageRequest(0, 2);
    tradeItem = new TradeItemDataBuilder().build();
    lot = new LotDataBuilder()
        .withTradeItem(tradeItem)
        .build();

    expected = Pagination.getPage(Arrays.asList(lot), pageable);
  }

  @Test
  public void searchShouldReturnRepositoryResult() {
    LotSearchParams lotSearchParams = new LotSearchParams(
        LocalDate.now(),
        tradeItem.getId(),
        lot.getLotCode(),
        Arrays.asList(lot.getId())
    );

    when(tradeItemRepository.findOne(eq(tradeItem.getId()))).thenReturn(tradeItem);

    when(lotRepository.search(
        eq(tradeItem),
        eq(lotSearchParams.getExpirationDate()),
        eq(lotSearchParams.getLotCode()),
        eq(lotSearchParams.getId()),
        eq(pageable)
    )).thenReturn(expected);

    Page<Lot> result = lotService.search(lotSearchParams, pageable);

    assertEquals(expected, result);
  }

  @Test
  public void searchShouldPassNullsToRepository() {
    LotSearchParams lotSearchParams = new LotSearchParams(
        LocalDate.now(),
        null,
        lot.getLotCode(),
        null
    );

    when(tradeItemRepository.findOne(eq(tradeItem.getId()))).thenReturn(tradeItem);

    when(lotRepository.search(
        eq(null),
        eq(lotSearchParams.getExpirationDate()),
        eq(lotSearchParams.getLotCode()),
        eq(null),
        eq(pageable)
    )).thenReturn(expected);

    Page<Lot> result = lotService.search(lotSearchParams, pageable);

    assertEquals(expected, result);
  }

  @Test(expected = ValidationMessageException.class)
  public void searchShouldThrowExceptionIfTradeItemDoesNotExist() {
    when(tradeItemRepository.findOne(eq(tradeItem.getId()))).thenReturn(null);

    LotSearchParams lotSearchParams = new LotSearchParams(
        LocalDate.now(),
        tradeItem.getId(),
        lot.getLotCode(),
        Arrays.asList(lot.getId())
    );

    lotService.search(lotSearchParams, pageable);
  }

  @Test(expected = NullPointerException.class)
  public void searchShouldThrowExceptionIfRequestParamsAreNotGiven() {
    lotService.search(null, pageable);
  }

}