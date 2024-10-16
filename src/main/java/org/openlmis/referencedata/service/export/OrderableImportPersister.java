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

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.util.EasyBatchUtils;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.TransactionUtils;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("orderable.csv")
public class OrderableImportPersister
    implements DataImportPersister<Orderable, OrderableDto, OrderableDto> {

  @Autowired private FileHelper fileHelper;
  @Autowired private OrderableRepository orderableRepository;
  @Autowired private TransactionUtils transactionUtils;

  @Autowired
  @Qualifier("importExecutorService")
  private ExecutorService importExecutorService;

  @Override
  public List<OrderableDto> processAndPersist(InputStream dataStream, Profiler profiler)
      throws InterruptedException {
    profiler.start("READ_CSV");
    List<OrderableDto> importedDtos = fileHelper.readCsv(OrderableDto.class, dataStream);

    profiler.start("CREATE_OR_UPDATE_SAVE_ALL");
    List<OrderableDto> result =
        new EasyBatchUtils(importExecutorService)
            .processInBatches(
                importedDtos,
                batch -> transactionUtils.runInOwnTransaction(() -> importBatch(batch)));

    profiler.start("RETURN");
    return result;
  }

  private List<OrderableDto> importBatch(List<OrderableDto> importedDtosBatch) {
    final List<Orderable> toPersistBatch = createOrUpdate(importedDtosBatch);
    final List<Orderable> persistedObjects = orderableRepository.saveAll(toPersistBatch);

    return OrderableDto.newInstances(persistedObjects);
  }

  private List<Orderable> createOrUpdate(List<OrderableDto> dtoList) {
    final ImportContext importContext = new ImportContext(dtoList);
    final List<Orderable> persistList = new LinkedList<>();

    for (OrderableDto dto : dtoList) {
      Orderable latestOrderable = importContext.orderableByCode.get(dto.getProductCode());

      if (!Orderable.isEqualForCsvFields(dto, latestOrderable)) {
        final Orderable orderable =
            (latestOrderable == null)
                ? Orderable.newInstance(dto)
                : Orderable.updateFrom(latestOrderable, dto);
        persistList.add(orderable);
      }
    }

    return persistList;
  }

  private class ImportContext {
    final Map<String, Orderable> orderableByCode;

    ImportContext(List<OrderableDto> dtoList) {
      final List<Code> distinctOrderableCodes =
          dtoList.stream()
              .map(OrderableDto::getProductCode)
              .filter(Objects::nonNull)
              .distinct()
              .map(Code::code)
              .collect(toList());

      orderableByCode =
          distinctOrderableCodes.isEmpty()
              ? emptyMap()
              : orderableRepository.findAllLatestByProductCode(distinctOrderableCodes).stream()
                  .collect(toMap(o -> o.getProductCode().toString(), Function.identity()));
    }
  }
}
