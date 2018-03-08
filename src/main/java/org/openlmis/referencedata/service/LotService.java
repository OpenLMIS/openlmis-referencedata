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

import static org.apache.commons.collections4.IterableUtils.isEmpty;

import java.util.Collections;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.LotRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class LotService {

  @Autowired
  private LotRepository lotRepository;

  @Autowired
  private TradeItemRepository tradeItemRepository;

  /**
   * Returns page of lots matching the given request parameters.
   *
   * @param requestParams  the request parameters (trade item ID, expiration date, code or IDs).
   * @param pageable  the page to get, or one page with all if null.
   * @return the Page of lots found, or an empty page.
   */
  public Page<Lot> search(@NotNull LotSearchParams requestParams, Pageable pageable) {

    List<TradeItem> tradeItems = getTradeItems(requestParams.getTradeItemId());

    return lotRepository.search(
        tradeItems,
        requestParams.getExpirationDate(),
        requestParams.getLotCode(),
        requestParams.getId(),
        pageable
    );
  }

  private List<TradeItem> getTradeItems(Collection<UUID> id) {
    if (isEmpty(id)) {
      return Collections.emptyList();
    }

    List<TradeItem> tradeItems = tradeItemRepository.findAll(id);
    if (tradeItems.size() != id.size()) {
      throw new ValidationMessageException(
          new Message(TradeItemMessageKeys.ERROR_NOT_FOUND_WITH_ID, id));
    }

    return tradeItems;
  }
}
