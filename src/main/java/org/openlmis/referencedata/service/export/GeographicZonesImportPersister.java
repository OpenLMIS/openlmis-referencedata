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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.dto.GeographicZoneDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.EasyBatchUtils;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.TransactionUtils;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("geographicZones.csv")
public class GeographicZonesImportPersister
    implements DataImportPersister<GeographicZone, GeographicZoneDto, GeographicZoneDto> {

  @Autowired private FileHelper fileHelper;
  @Autowired private GeographicZoneRepository geographicZoneRepository;
  @Autowired private GeographicLevelRepository geographicLevelRepository;
  @Autowired private TransactionUtils transactionUtils;

  @Value("${referencedata.catchmentPopulationAutoCalc.enabled}")
  private boolean catchmentPopulationAutoCalc;

  @Autowired
  @Qualifier("importExecutorService")
  private ExecutorService importExecutorService;

  @Override
  public List<GeographicZoneDto> processAndPersist(InputStream dataStream, Profiler profiler)
      throws InterruptedException {
    profiler.start("READ CSV");
    List<GeographicZoneDto> importedDtos = fileHelper.readCsv(GeographicZoneDto.class, dataStream)
        .stream().filter(dto -> dto.getCode() != null).collect(Collectors.toList());

    List<GeographicLevel> allGeoLevels = StreamSupport.stream(geographicLevelRepository.findAll()
        .spliterator(), false).collect(Collectors.toList());

    profiler.start("CREATE_OR_UPDATE_SAVE_ALL");
    List<GeographicZoneDto> result = new EasyBatchUtils(importExecutorService)
        .processInBatches(
            importedDtos,
            batch -> transactionUtils.runInOwnTransaction(() ->
                importBatch(batch, allGeoLevels)));

    profiler.start("RETURN");
    return result;
  }

  private List<GeographicZoneDto> importBatch(List<GeographicZoneDto> importedDtosBatch,
                                              List<GeographicLevel> geoLevels) {
    List<GeographicZone> toPersistBatch = createListToUpdate(importedDtosBatch, geoLevels);
    List<GeographicZone> persistedObjects = new ArrayList<>();
    geographicZoneRepository.saveAll(toPersistBatch).forEach(persistedObjects::add);

    return GeographicZoneDto.newInstances(persistedObjects);
  }

  private List<GeographicZone> createListToUpdate(List<GeographicZoneDto> dtoList,
                                                  List<GeographicLevel> geoLevels) {
    List<GeographicZone> persistList = new LinkedList<>();

    for (GeographicZoneDto dto : dtoList) {
      GeographicZone zone = geographicZoneRepository.findByCode(dto.getCode());
      if (zone != null) {
        validateZone(zone, geoLevels);
        zone.setCatchmentPopulation(dto.getCatchmentPopulation());
        persistList.add(zone);
      }
    }

    return persistList;
  }

  private void validateZone(GeographicZone zone, List<GeographicLevel> geoLevels) {
    int lowestLevelNumber = Collections.max(geoLevels,
        Comparator.comparing(GeographicLevel::getLevelNumber)).getLevelNumber();
    if (catchmentPopulationAutoCalc && zone.getLevel().getLevelNumber() != lowestLevelNumber) {
      throw new ValidationMessageException(
          GeographicZoneMessageKeys.ERROR_TRYING_TO_UPDATE_NON_LOWEST_GEOGRAPHIC_ZONE);
    }
  }
}
