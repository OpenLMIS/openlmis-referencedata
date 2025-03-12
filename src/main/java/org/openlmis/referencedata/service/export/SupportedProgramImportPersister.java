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
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.domain.SupportedProgramPrimaryKey;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.dto.SupportedProgramCsvModel;
import org.openlmis.referencedata.dto.SupportedProgramDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupportedProgramRepository;
import org.openlmis.referencedata.util.EasyBatchUtils;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.TransactionUtils;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service(SupportedProgramImportPersister.SUPPORTED_PROGRAM_FILE_NAME)
public class SupportedProgramImportPersister
    implements DataImportPersister<
        SupportedProgram, SupportedProgramCsvModel, SupportedProgramDto> {

  public static final String SUPPORTED_PROGRAM_FILE_NAME = "supportedProgram.csv";

  @Autowired private FileHelper fileHelper;
  @Autowired private SupportedProgramRepository supportedProgramRepository;
  @Autowired private FacilityRepository facilityRepository;
  @Autowired private ProgramRepository programRepository;
  @Autowired private TransactionUtils transactionUtils;

  @Autowired
  @Qualifier("importExecutorService")
  private ExecutorService importExecutorService;

  @Override
  public ImportResponseDto.ImportDetails processAndPersist(InputStream dataStream,
                                                           Profiler profiler)
      throws InterruptedException {
    profiler.start("READ_CSV");
    List<SupportedProgramCsvModel> importedDtos =
        fileHelper.readCsv(SupportedProgramCsvModel.class, dataStream);

    profiler.start("CREATE_OR_UPDATE_SAVE_ALL");
    List<SupportedProgramDto> result =
        new EasyBatchUtils(importExecutorService)
            .processInBatches(
                importedDtos,
                batch -> transactionUtils.runInOwnTransaction(() -> importBatch(batch)));

    profiler.start("RETURN");
    return new ImportResponseDto.ImportDetails(
        SUPPORTED_PROGRAM_FILE_NAME,
        importedDtos.size(),
        result.size(),
        0,
        new ArrayList<>()
    );
  }

  private List<SupportedProgramDto> importBatch(List<SupportedProgramCsvModel> importedDtosBatch) {
    final List<SupportedProgram> toPersistBatch = createOrUpdate(importedDtosBatch);
    final List<SupportedProgram> persistedObjects =
        supportedProgramRepository.saveAll(toPersistBatch);

    return SupportedProgramDto.newInstances(persistedObjects);
  }

  private List<SupportedProgram> createOrUpdate(List<SupportedProgramCsvModel> dtoList) {
    final ImportContext importContext = new ImportContext(dtoList);
    final List<SupportedProgram> persistList = new LinkedList<>();

    for (SupportedProgramCsvModel dto : dtoList) {
      final Facility facility =
          ofNullable(importContext.facilityByCode.get(dto.getFacilityCode()))
              .orElseThrow(
                  () -> new ValidationMessageException(FacilityMessageKeys.ERROR_NOT_FOUND));
      final Program program =
          ofNullable(importContext.programByCode.get(dto.getProgramCode()))
              .orElseThrow(
                  () -> new ValidationMessageException(ProgramMessageKeys.ERROR_NOT_FOUND));

      Optional<SupportedProgram> latestSupportedProgram =
          ofNullable(
              importContext.supportedProgramByPrimaryKey.get(
                  new SupportedProgramPrimaryKey(facility, program)));

      if (latestSupportedProgram.isPresent()) {
        latestSupportedProgram.get().updateFrom(dto);
        persistList.add(latestSupportedProgram.get());
      } else {
        persistList.add(
            new SupportedProgram(
                new SupportedProgramPrimaryKey(facility, program),
                dto.getActive(),
                dto.getLocallyFulfilled(),
                dto.getStartDate()));
      }
    }

    return persistList;
  }

  private class ImportContext {
    final Map<String, Facility> facilityByCode;
    final Map<String, Program> programByCode;
    final Map<SupportedProgramPrimaryKey, SupportedProgram> supportedProgramByPrimaryKey;

    ImportContext(List<SupportedProgramCsvModel> dtoList) {
      final List<String> distinctFacilityCodes =
          dtoList.stream()
              .map(SupportedProgramCsvModel::getFacilityCode)
              .filter(Objects::nonNull)
              .distinct()
              .collect(toList());
      final List<Code> distinctProgramCodes =
          dtoList.stream()
              .map(SupportedProgramCsvModel::getProgramCode)
              .filter(Objects::nonNull)
              .distinct()
              .map(Code::code)
              .collect(toList());

      facilityByCode =
          distinctFacilityCodes.isEmpty()
              ? emptyMap()
              : facilityRepository.findAllByCodeIn(distinctFacilityCodes).stream()
                  .collect(toMap(Facility::getCode, Function.identity()));

      programByCode =
          distinctProgramCodes.isEmpty()
              ? emptyMap()
              : programRepository.findAllByCodeIn(distinctProgramCodes).stream()
                  .collect(toMap(p -> p.getCode().toString(), Function.identity()));

      final List<SupportedProgramPrimaryKey> distinctSupportedProgramPrimaryKeys =
          dtoList.stream()
              .map(
                  dto ->
                      new SupportedProgramPrimaryKey(
                          facilityByCode.get(dto.getFacilityCode()),
                          programByCode.get(dto.getProgramCode())))
              .collect(toList());

      supportedProgramByPrimaryKey =
          distinctSupportedProgramPrimaryKeys.isEmpty()
              ? emptyMap()
              : supportedProgramRepository.findAllById(distinctSupportedProgramPrimaryKeys).stream()
                  .collect(toMap(SupportedProgram::getFacilityProgram, Function.identity()));
    }
  }
}
