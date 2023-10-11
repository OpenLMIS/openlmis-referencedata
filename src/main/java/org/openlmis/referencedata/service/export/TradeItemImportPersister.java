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

package org.openlmis.referencedata.service.export;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.dto.TradeItemCsvModel;
import org.openlmis.referencedata.dto.TradeItemDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.TradeItemRepository;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("tradeItem.csv")
public class TradeItemImportPersister implements DataImportPersister<Orderable,
    TradeItemCsvModel, OrderableDto> {

  @Autowired
  private FileHelper fileHelper;

  @Autowired
  private TradeItemRepository tradeItemRepository;

  @Autowired
  private OrderableRepository orderableRepository;

  @Override
  public List<OrderableDto> processAndPersist(InputStream dataStream) {
    List<TradeItemCsvModel> importedDtos =
        fileHelper.readCsv(TradeItemCsvModel.class, dataStream);
    List<Orderable> persistedObjects = orderableRepository.saveAll(
        createOrUpdate(importedDtos)
    );

    return new ArrayList<>(OrderableDto.newInstance(persistedObjects));
  }

  @Override
  public Set<Orderable> createOrUpdate(List<TradeItemCsvModel> dtoList) {
    Map<Orderable, TradeItem> tradeItemPersistMap = prepareTradeItems(dtoList);
    List<TradeItem> tradeItems = tradeItemRepository.saveAll(tradeItemPersistMap.values());

    Iterator<Map.Entry<Orderable, TradeItem>> tradeItemMapIterator = tradeItemPersistMap
        .entrySet().iterator();
    Iterator<TradeItem> updatedTradeItemsIterator = tradeItems.iterator();

    Map<Orderable, TradeItem> updatedMap = new LinkedHashMap<>();
    while (tradeItemMapIterator.hasNext()) {
      Map.Entry<Orderable, TradeItem> entry = tradeItemMapIterator.next();
      TradeItem updatedTradeItem = updatedTradeItemsIterator.next();
      entry.setValue(updatedTradeItem);
      updatedMap.put(entry.getKey(), entry.getValue());
    }

    return prepareOrderables(updatedMap);
  }

  private Set<Orderable> prepareOrderables(Map<Orderable, TradeItem> entityMap) {
    Set<Orderable> orderablePersistList = new HashSet<>();

    for (Map.Entry<Orderable, TradeItem> entry : entityMap.entrySet()) {
      Orderable orderable = entry.getKey();
      TradeItem tradeItem = entry.getValue();

      UUID tradeItemId = tradeItem.getId();
      Map<String, String> identifiers = new HashMap<>();
      identifiers.put("tradeItem", tradeItemId.toString());
      orderable.setIdentifiers(identifiers);

      orderablePersistList.add(orderable);
    }

    return orderablePersistList;
  }

  private Map<Orderable, TradeItem> prepareTradeItems(List<TradeItemCsvModel> dtoList) {
    Map<Orderable, TradeItem> tradeItemPersistMap = new LinkedHashMap<>();

    for (TradeItemCsvModel dto: dtoList) {
      Orderable orderable = orderableRepository
          .findFirstByProductCodeOrderByIdentityVersionNumberDesc(Code.code(dto.getCode()));

      if (orderable == null) {
        throw new NotFoundException(new Message(
            "Orderable with code: " + dto.getCode() + " not found!"));
      }

      TradeItem tradeItem;
      Map<String, String> identifiers = orderable.getIdentifiers();

      if (identifiers == null || !identifiers.containsKey("tradeItem")) {
        TradeItemDto tradeItemDto = new TradeItemDto();
        tradeItemDto.setManufacturerOfTradeItem(dto.getManufacturerOfTradeItem());
        tradeItem = TradeItem.newInstance(tradeItemDto);
      } else {
        String tradeItemIdentifier = identifiers.get("tradeItem");
        tradeItem = tradeItemRepository.findById(UUID.fromString(tradeItemIdentifier))
            .orElseThrow(() -> new NotFoundException(
                "Could not find trade item with id: " + tradeItemIdentifier));
        tradeItem.setManufacturerOfTradeItem(dto.getManufacturerOfTradeItem());
      }

      tradeItemPersistMap.put(orderable, tradeItem);
    }

    return tradeItemPersistMap;
  }

}
