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

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.TradeItemCsvModel;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.service.export.ExportableDataService;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TradeItemService implements ExportableDataService<TradeItemCsvModel> {

  @Autowired
  private TradeItemRepository tradeItemRepository;

  @Override
  public List<TradeItemCsvModel> findAllExportableItems() {
    return tradeItemRepository.findAllTradeItemCsvModels();
  }

  @Override
  public Class<TradeItemCsvModel> getExportableType() {
    return TradeItemCsvModel.class;
  }

  /**
   * Searches for trade items matching the given parameters. Filters are applied with the following
   * precedence: if any {@code id} is provided, trade items are looked up by those identifiers and
   * the remaining filters are ignored; otherwise, if a {@code classificationId} is provided, it is
   * matched either fully or partially depending on the {@code fullMatch} flag; when no filter is
   * provided, all trade items are returned.
   *
   * @param requestParams the search parameters (trade item ids, classification id, full match flag)
   * @param pageable      the pagination parameters
   * @return a page of matching trade items
   */
  public Page<TradeItem> search(@NotNull TradeItemSearchParams requestParams, Pageable pageable) {
    final Set<UUID> id = Optional.ofNullable(requestParams.getId()).orElse(Collections.emptySet());
    if (!id.isEmpty()) {
      return Pagination.getPage(tradeItemRepository.findAllById(id), pageable);
    }

    String classificationId = requestParams.getClassificationId();
    if (StringUtils.isBlank(classificationId)) {
      return tradeItemRepository.findAll(pageable);
    }
    Iterable<TradeItem> result = requestParams.isFullMatch()
        ? tradeItemRepository.findByClassificationId(classificationId)
        : tradeItemRepository.findByClassificationIdLike(classificationId);
    return Pagination.getPage(Lists.newArrayList(result), pageable);
  }

}
