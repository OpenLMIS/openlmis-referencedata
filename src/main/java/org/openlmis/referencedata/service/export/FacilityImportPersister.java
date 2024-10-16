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
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.dto.BasicFacilityDto;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.dto.FacilityOperatorDto;
import org.openlmis.referencedata.dto.FacilityTypeDto;
import org.openlmis.referencedata.dto.GeographicZoneSimpleDto;
import org.openlmis.referencedata.dto.MinimalFacilityDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityOperatorRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.EasyBatchUtils;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.TransactionUtils;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeMessageKeys;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("facility.csv")
public class FacilityImportPersister
    implements DataImportPersister<Facility, FacilityDto, FacilityDto> {

  @Autowired private FileHelper fileHelper;
  @Autowired private FacilityRepository facilityRepository;
  @Autowired private GeographicZoneRepository geographicZoneRepository;
  @Autowired private FacilityTypeRepository facilityTypeRepository;
  @Autowired private FacilityOperatorRepository facilityOperatorRepository;
  @Autowired private TransactionUtils transactionUtils;

  @Autowired
  @Qualifier("importExecutorService")
  private ExecutorService importExecutorService;

  @Override
  public List<FacilityDto> processAndPersist(InputStream dataStream, Profiler profiler)
      throws InterruptedException {
    profiler.start("READ_CSV");
    List<FacilityDto> importedDtos = fileHelper.readCsv(FacilityDto.class, dataStream);

    profiler.start("CREATE_OR_UPDATE_SAVE_ALL");
    List<FacilityDto> result =
        new EasyBatchUtils(importExecutorService)
            .processInBatches(
                importedDtos,
                batch -> transactionUtils.runInOwnTransaction(() -> importBatch(batch)));

    profiler.start("RETURN");
    return result;
  }

  private List<FacilityDto> importBatch(List<FacilityDto> importedDtosBatch) {
    final List<Facility> toPersistBatch = createOrUpdate(importedDtosBatch);
    final List<Facility> persistedObjects = facilityRepository.saveAll(toPersistBatch);

    return FacilityDto.newInstances(persistedObjects);
  }

  private List<Facility> createOrUpdate(List<FacilityDto> dtoList) {
    final ImportContext importContext = new ImportContext(dtoList);
    final List<Facility> persistList = new LinkedList<>();

    for (FacilityDto dto : dtoList) {
      Optional<Facility> latestFacility = facilityRepository.findByCode(dto.getCode());

      if (latestFacility.isPresent()) {
        latestFacility.get().updateFrom(dto);
        persistList.add(latestFacility.get());
      } else {
        persistList.add(build(importContext, dto));
      }
    }

    return persistList;
  }

  private Facility build(ImportContext importContext, FacilityDto importer) {
    final GeographicZone geographicZone =
        Optional.ofNullable(importer.getGeographicZone())
            .map(GeographicZoneSimpleDto::getCode)
            .map(importContext.geographicZoneByCode::get)
            .orElseThrow(
                () -> new ValidationMessageException(GeographicZoneMessageKeys.ERROR_NOT_FOUND));
    final FacilityType facilityType =
        Optional.ofNullable(importer.getType())
            .map(FacilityTypeDto::getCode)
            .map(importContext.facilityTypeByCode::get)
            .orElseThrow(
                () -> new ValidationMessageException(FacilityTypeMessageKeys.ERROR_NOT_FOUND));
    final FacilityOperator facilityOperator =
        Optional.ofNullable(importer.getOperator())
            .map(FacilityOperatorDto::getCode)
            .map(importContext.facilityOperatorByCode::get)
            .orElse(null);

    final Facility facility =
        Optional.ofNullable(importer.getCode())
            .map(importContext.facilityByCode::get)
            .orElseGet(Facility::new);
    facility.updateFrom(importer);
    facility.setGeographicZone(geographicZone);
    facility.setType(facilityType);
    facility.setOperator(facilityOperator);
    return facility;
  }

  private class ImportContext {
    final Map<String, GeographicZone> geographicZoneByCode;
    final Map<String, FacilityType> facilityTypeByCode;
    final Map<String, FacilityOperator> facilityOperatorByCode;
    final Map<String, Facility> facilityByCode;

    ImportContext(List<FacilityDto> dtoList) {
      final List<String> distinctGeographicZoneCodes =
          dtoList.stream()
              .map(BasicFacilityDto::getGeographicZone)
              .filter(Objects::nonNull)
              .map(GeographicZoneSimpleDto::getCode)
              .distinct()
              .collect(toList());
      final List<String> distinctFacilityTypeCodes =
          dtoList.stream()
              .map(BasicFacilityDto::getType)
              .filter(Objects::nonNull)
              .map(FacilityTypeDto::getCode)
              .distinct()
              .collect(toList());
      final List<String> distinctFacilityOperatorCodes =
          dtoList.stream()
              .map(BasicFacilityDto::getOperator)
              .filter(Objects::nonNull)
              .map(FacilityOperator.Importer::getCode)
              .distinct()
              .collect(toList());
      final List<String> distinctFacilityCodes =
          dtoList.stream().map(MinimalFacilityDto::getCode).distinct().collect(toList());

      geographicZoneByCode =
          distinctGeographicZoneCodes.isEmpty()
              ? emptyMap()
              : geographicZoneRepository.findAllByCodeIn(distinctGeographicZoneCodes).stream()
                  .collect(toMap(GeographicZone::getCode, Function.identity()));
      facilityTypeByCode =
          distinctFacilityTypeCodes.isEmpty()
              ? emptyMap()
              : facilityTypeRepository.findAllByCodeIn(distinctFacilityTypeCodes).stream()
                  .collect(toMap(FacilityType::getCode, Function.identity()));
      facilityOperatorByCode =
          distinctFacilityOperatorCodes.isEmpty()
              ? emptyMap()
              : facilityOperatorRepository.findAllByCodeIn(distinctFacilityOperatorCodes).stream()
                  .collect(toMap(FacilityOperator::getCode, Function.identity()));
      facilityByCode =
          distinctFacilityCodes.isEmpty()
              ? emptyMap()
              : facilityRepository.findAllByCodeIn(distinctFacilityCodes).stream()
                  .collect(toMap(Facility::getCode, Function.identity()));
    }
  }
}
