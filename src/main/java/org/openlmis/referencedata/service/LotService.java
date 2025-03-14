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

import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.LotRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.repository.lot.LotRepositorySearchParams;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.LotMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LotService {

  @Autowired
  private LotRepository lotRepository;

  @Autowired
  private TradeItemRepository tradeItemRepository;

  @Autowired
  private OrderableRepository orderableRepository;

  /**
   * Returns page of lots matching the given request parameters.
   *
   * @param requestParams  the request parameters (trade item ID, expiration date, code or IDs).
   * @param pageable  the page to get, or one page with all if null.
   * @return the Page of lots found, or an empty page.
   */
  public Page<Lot> search(@NotNull LotSearchParams requestParams, Pageable pageable) {
    final Set<UUID> tradeItemId = Optional.ofNullable(requestParams.getTradeItemId())
            .orElse(Collections.emptySet());
    final Set<UUID> orderableId = Optional.ofNullable(requestParams.getOrderableId())
            .orElse(Collections.emptySet());

    if (!tradeItemId.isEmpty() && !orderableId.isEmpty()) {
      throw new ValidationMessageException(
              LotMessageKeys.ERROR_ORDERABLE_ID_AND_TRADE_ITEM_ID_USED_TOGETHER
      );
    }

    final List<TradeItem> tradeItems;

    if (!orderableId.isEmpty()) {
      Page<Orderable> orderables = orderableRepository.findAllLatestByIds(
          requestParams.getOrderableId(),
          null
      );

      final Set<UUID> orderableTradeItems = orderables.getContent().stream()
          .filter(o -> Objects.nonNull(o.getTradeItemIdentifier()))
          .map(o -> UUID.fromString(o.getTradeItemIdentifier()))
          .collect(toSet());

      tradeItems = tradeItemRepository.findAllById(orderableTradeItems);

      if (tradeItems.isEmpty()) {
        return Pagination.getEmptyPage(pageable);
      }
    } else if (!tradeItemId.isEmpty() && !requestParams.isTradeItemIdIgnored()) {
      tradeItems = tradeItemRepository.findAllById(tradeItemId);

      if (tradeItems.isEmpty()) {
        return Pagination.getEmptyPage(pageable);
      }
    } else {
      tradeItems = Collections.emptyList();
    }

    return lotRepository.search(
        new LotRepositorySearchParams(
            new HashSet<>(tradeItems),
            requestParams.getExpirationDate(),
            requestParams.getExactCode(),
            requestParams.getLotCode(),
            requestParams.getId(),
            requestParams.getExpirationDateFrom(),
            requestParams.getExpirationDateTo()),
        pageable);
  }
}
