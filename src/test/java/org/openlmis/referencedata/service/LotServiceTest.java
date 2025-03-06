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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.LotRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.repository.lot.LotRepositorySearchParams;
import org.openlmis.referencedata.testbuilder.LotDataBuilder;
import org.openlmis.referencedata.testbuilder.OrderableDataBuilder;
import org.openlmis.referencedata.testbuilder.TradeItemDataBuilder;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RunWith(MockitoJUnitRunner.class)
public class LotServiceTest {

  @Mock
  private LotRepository lotRepository;

  @Mock
  private TradeItemRepository tradeItemRepository;

  @Mock
  private OrderableRepository orderableRepository;

  @InjectMocks
  private LotService lotService = new LotService();

  private Pageable pageable;

  private TradeItem tradeItem;

  private Lot lot;

  private Page<Lot> expected;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    pageable = PageRequest.of(0, 2);
    tradeItem = new TradeItemDataBuilder().build();
    lot = new LotDataBuilder()
        .withTradeItem(tradeItem)
        .build();

    expected = Pagination.getPage(ImmutableList.of(lot), pageable);
  }

  @Test
  public void searchShouldReturnRepositoryResult() {
    Set<UUID> tradeItemIds = singleton(tradeItem.getId());
    LotSearchParams lotSearchParams =
        new LotSearchParams(
            LocalDate.now(),
            tradeItemIds,
            null,
            lot.getLotCode(),
            singleton(lot.getId()),
            null,
            null,
            emptySet(),
            false);

    Set<TradeItem> tradeItems = singleton(tradeItem);
    when(tradeItemRepository.findAllById(tradeItemIds))
        .thenReturn(new ArrayList<>(tradeItems));

    when(lotRepository.search(
            new LotRepositorySearchParams(
                tradeItems,
                lotSearchParams.getExpirationDate(),
                null,
                lotSearchParams.getLotCode(),
                lotSearchParams.getId(),
                null,
                null),
            pageable))
        .thenReturn(expected);

    Page<Lot> result = lotService.search(lotSearchParams, pageable);

    assertEquals(expected, result);
  }

  @Test
  public void searchShouldPassNullsToRepository() {
    LotSearchParams lotSearchParams = new LotSearchParams(
        LocalDate.now(),
        null,
        null,
        lot.getLotCode(),
        null,
        null,
        null,
        emptySet(),
            false
    );

    when(lotRepository.search(
            new LotRepositorySearchParams(
                emptySet(),
                lotSearchParams.getExpirationDate(),
                null,
                lotSearchParams.getLotCode(),
                null,
                null,
                null),
            pageable))
        .thenReturn(expected);

    Page<Lot> result = lotService.search(lotSearchParams, pageable);

    assertEquals(expected, result);
  }

  @Test
  public void searchShouldReturnEmptyListIfTradeItemDoesNotExist() {
    when(tradeItemRepository.findAllById(singleton(tradeItem.getId()))).thenReturn(emptyList());

    LotSearchParams lotSearchParams = new LotSearchParams(
        LocalDate.now(),
        singleton(tradeItem.getId()),
        emptySet(),
        lot.getLotCode(),
        singleton(lot.getId()),
            null,
            null,
            null,
            false
    );

    Page<Lot> result = lotService.search(lotSearchParams, pageable);
    assertThat(result.getContent(), hasSize(0));
  }

  @Test(expected = NullPointerException.class)
  public void searchShouldThrowExceptionIfRequestParamsAreNotGiven() {
    lotService.search(null, pageable);
  }

  @Test
  public void searchShouldNotThrowExceptionIfRequestParamsAreNotGiven() {
    when(lotRepository.search(new LotRepositorySearchParams(
            emptySet(),
            null,
            null,
            null,
            null,
            null,
            null),
            pageable
    )).thenReturn(expected);

    LotSearchParams lotSearchParams =
        new LotSearchParams(null, null, null, null, null, null, null, null, true);

    Page<Lot> result = lotService.search(lotSearchParams, pageable);
    assertThat(result.getContent(), hasSize(1));
  }

  @Test(expected = ValidationMessageException.class)
  public void searchShouldThrowValidationExceptionWhenTradeItemIdAndOrderableIdIsSet() {
    LotSearchParams lotSearchParams =
        new LotSearchParams(
            null,
            singleton(UUID.randomUUID()),
            null,
            null,
            null,
            null,
            null,
            singleton(UUID.randomUUID()),
            false);

    Page<Lot> result = lotService.search(lotSearchParams, pageable);
    assertThat(result.getContent(), hasSize(0));
  }

  @Test
  public void shouldFindByOrderableId() {
    final UUID orderableId = UUID.fromString("0c6a0222-9b04-4519-a4e1-ab041a23cf3b");
    final UUID tradeItemIdentifier = UUID.fromString("0c6a0222-9b04-4519-a4e1-ab041a23cf3c");
    final Orderable orderable =
        new OrderableDataBuilder()
            .withId(orderableId)
            .withIdentifier(Orderable.TRADE_ITEM, tradeItemIdentifier)
            .build();
    final TradeItem tradeItem = new TradeItemDataBuilder().withId(tradeItemIdentifier).build();
    final Page<Lot> expectedResult = mock(Page.class);

    when(orderableRepository.findAllLatestByIds(singleton(orderableId), null))
        .thenReturn(new PageImpl<>(singletonList(orderable)));
    when(tradeItemRepository.findAllById(singleton(tradeItemIdentifier)))
        .thenReturn(singletonList(tradeItem));
    when(lotRepository.search(
            new LotRepositorySearchParams(singleton(tradeItem), null, null, null, null, null, null),
            pageable))
        .thenReturn(expectedResult);

    final LotSearchParams lotSearchParams =
        new LotSearchParams(
            null, null, null, null, null, null, null, singleton(orderableId), false);

    final Page<Lot> result = lotService.search(lotSearchParams, pageable);
    assertEquals(expectedResult, result);
  }
}
