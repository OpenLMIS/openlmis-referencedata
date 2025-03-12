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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import lombok.EqualsAndHashCode;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.dto.ProgramOrderableCsvModel;
import org.openlmis.referencedata.dto.ProgramOrderableDto;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramOrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.util.EasyBatchUtils;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.TransactionUtils;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service(ProgramOrderableImportPersister.PROGRAM_ORDERABLE_FILE_NAME)
public class ProgramOrderableImportPersister
    implements DataImportPersister<
        ProgramOrderable, ProgramOrderableCsvModel, ProgramOrderableDto> {

  public static final String PROGRAM_ORDERABLE_FILE_NAME = "programOrderable.csv";

  @Value("${currencyCode}")
  private String currencyCode;

  @Autowired private FileHelper fileHelper;
  @Autowired private ProgramOrderableRepository programOrderableRepository;
  @Autowired private ProgramRepository programRepository;
  @Autowired private OrderableRepository orderableRepository;
  @Autowired private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;
  @Autowired private TransactionUtils transactionUtils;

  @Autowired
  @Qualifier("importExecutorService")
  private ExecutorService importExecutorService;

  @Override
  public ImportResponseDto.ImportDetails processAndPersist(InputStream dataStream,
                                                           Profiler profiler)
      throws InterruptedException {
    profiler.start("READ_CSV");
    List<ProgramOrderableCsvModel> importedDtos =
        fileHelper.readCsv(ProgramOrderableCsvModel.class, dataStream);

    profiler.start("CREATE_OR_UPDATE_SAVE_ALL");
    List<ProgramOrderableDto> result =
        new EasyBatchUtils(importExecutorService)
            .processInBatches(
                importedDtos,
                batch -> transactionUtils.runInOwnTransaction(() -> importBatch(batch)));

    profiler.start("RETURN");
    return new ImportResponseDto.ImportDetails(
        PROGRAM_ORDERABLE_FILE_NAME,
        importedDtos.size(),
        result.size(),
        0,
        new ArrayList<>()
    );
  }

  private List<ProgramOrderableDto> importBatch(List<ProgramOrderableCsvModel> importedDtosBatch) {
    final List<ProgramOrderable> toPersistBatch = createOrUpdate(importedDtosBatch);
    final List<ProgramOrderable> persistedObjects =
        programOrderableRepository.saveAll(toPersistBatch);

    return new ArrayList<>(ProgramOrderableDto.newInstance(persistedObjects));
  }

  private List<ProgramOrderable> createOrUpdate(List<ProgramOrderableCsvModel> dtoList) {
    final ImportContext context = new ImportContext(dtoList);
    final List<ProgramOrderable> persistList = new LinkedList<>();

    for (ProgramOrderableCsvModel dto : dtoList) {
      Program program = context.programByCode.get(dto.getProgramCode());
      Orderable orderable = context.orderableByCode.get(dto.getOrderableCode());
      OrderableDisplayCategory orderableDisplayCategory =
          context.orderableDisplayCategoryByCode.get(dto.getCategoryCode());

      ProgramOrderableDto programOrderableDto =
          new ProgramOrderableDto(
              program.getId(),
              orderableDisplayCategory.getId(),
              orderableDisplayCategory.getOrderedDisplayValue().getDisplayName(),
              orderableDisplayCategory.getOrderedDisplayValue().getDisplayOrder(),
              dto.isActive(),
              dto.isFullSupply(),
              dto.getDisplayOrder(),
              dto.getDosesPerPatient(),
              dto.getPricePerPack() != null
                  ? Money.of(
                      CurrencyUnit.of(currencyCode), Double.parseDouble(dto.getPricePerPack()))
                  : null,
              null);

      ProgramOrderable programOrderable =
          context.programOrderableIdentityByCodes.get(
              new ProgramOrderableIdentity(
                  program.getCode(),
                  orderable.getProductCode(),
                  orderableDisplayCategory.getCode()));

      if (programOrderable == null) {
        programOrderable =
            ProgramOrderable.createNew(
                program, orderableDisplayCategory, orderable, CurrencyUnit.of(currencyCode));
      }

      programOrderable.updateFrom(programOrderableDto);
      persistList.add(programOrderable);
    }

    return persistList;
  }

  @EqualsAndHashCode
  private static class ProgramOrderableIdentity {
    private final Code programCode;
    private final Code orderableCode;
    private final Code orderableDisplayCategoryCode;

    ProgramOrderableIdentity(ProgramOrderable programOrderable) {
      programCode = programOrderable.getProgram().getCode();
      orderableCode = programOrderable.getProduct().getProductCode();
      orderableDisplayCategoryCode = programOrderable.getOrderableDisplayCategory().getCode();
    }

    ProgramOrderableIdentity(
        Code programCode, Code orderableCode, Code orderableDisplayCategoryCode) {
      this.programCode = programCode;
      this.orderableCode = orderableCode;
      this.orderableDisplayCategoryCode = orderableDisplayCategoryCode;
    }
  }

  private class ImportContext {
    final Map<String, Program> programByCode;
    final Map<String, Orderable> orderableByCode;
    final Map<String, OrderableDisplayCategory> orderableDisplayCategoryByCode;
    final Map<ProgramOrderableIdentity, ProgramOrderable> programOrderableIdentityByCodes;

    ImportContext(List<ProgramOrderableCsvModel> dtoList) {
      final List<Code> distinctProgramCodes =
          dtoList.stream()
              .map(ProgramOrderableCsvModel::getProgramCode)
              .filter(Objects::nonNull)
              .distinct()
              .map(Code::code)
              .collect(toList());
      final List<Code> distinctOrderableCodes =
          dtoList.stream()
              .map(ProgramOrderableCsvModel::getOrderableCode)
              .filter(Objects::nonNull)
              .distinct()
              .map(Code::code)
              .collect(toList());
      final List<Code> distinctCategoryCodes =
          dtoList.stream()
              .map(ProgramOrderableCsvModel::getCategoryCode)
              .filter(Objects::nonNull)
              .distinct()
              .map(Code::code)
              .collect(toList());

      programByCode =
          distinctProgramCodes.isEmpty()
              ? emptyMap()
              : programRepository.findAllByCodeIn(distinctProgramCodes).stream()
                  .collect(toMap(p -> p.getCode().toString(), Function.identity()));

      orderableByCode =
          distinctOrderableCodes.isEmpty()
              ? emptyMap()
              : orderableRepository.findAllLatestByProductCode(distinctOrderableCodes).stream()
                  .collect(toMap(o -> o.getProductCode().toString(), Function.identity()));

      orderableDisplayCategoryByCode =
          distinctCategoryCodes.isEmpty()
              ? emptyMap()
              : orderableDisplayCategoryRepository.findAllByCodeIn(distinctCategoryCodes).stream()
                  .collect(toMap(c -> c.getCode().toString(), Function.identity()));

      programOrderableIdentityByCodes =
          programOrderableRepository
              .findAllByProgramCodeInAndProductCodeInAndOrderableDisplayCategoryCodeIn(
                  distinctProgramCodes.stream().map(Code::toString).collect(toList()),
                  distinctOrderableCodes.stream().map(Code::toString).collect(toList()),
                  distinctCategoryCodes.stream().map(Code::toString).collect(toList()))
              .stream()
              .collect(
                  toMap(
                      ProgramOrderableIdentity::new,
                      Function.identity(),
                      (first, second) ->
                          first.getProduct().getVersionNumber()
                                  > second.getProduct().getVersionNumber()
                              ? first
                              : second));
    }
  }
}
