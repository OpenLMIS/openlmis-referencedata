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
import java.util.LinkedList;
import java.util.List;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.OrderableBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("orderable.csv")
public class OrderableImportPersister
    implements DataImportPersister<Orderable, OrderableDto, OrderableDto> {

  @Autowired
  private FileHelper fileHelper;

  @Autowired
  private OrderableBuilder orderableBuilder;

  @Autowired
  private OrderableRepository orderableRepository;

  @Override
  public List<OrderableDto> processAndPersist(InputStream dataStream) {
    List<OrderableDto> importedDtos = fileHelper.readCsv(OrderableDto.class, dataStream);
    List<Orderable> persistedObjects = orderableRepository.saveAll(
        createOrUpdate(importedDtos));

    return OrderableDto.newInstances(persistedObjects);
  }

  @Override
  public List<Orderable> createOrUpdate(List<OrderableDto> dtoList) {
    List<Orderable> persistList = new LinkedList<>();
    for (OrderableDto dto: dtoList) {
      Orderable latestOrderable = orderableRepository
          .findFirstByProductCodeOrderByIdentityVersionNumberDesc(
              Code.code(dto.getProductCode()));

      if (!Orderable.isEqualForCsvFields(dto, latestOrderable)) {
        persistList.add(orderableBuilder.newOrderable(dto, latestOrderable));
      }
    }

    return persistList;
  }

}
