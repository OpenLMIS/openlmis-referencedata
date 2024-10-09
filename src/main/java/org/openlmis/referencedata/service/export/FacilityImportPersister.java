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
import java.util.Optional;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.dto.FacilityOperatorDto;
import org.openlmis.referencedata.dto.FacilityTypeDto;
import org.openlmis.referencedata.dto.GeographicZoneSimpleDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityOperatorRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.messagekeys.FacilityOperatorMessageKeys;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeMessageKeys;
import org.openlmis.referencedata.util.messagekeys.GeographicZoneMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("facility.csv")
public class FacilityImportPersister
    implements DataImportPersister<Facility, FacilityDto, FacilityDto> {

  @Autowired private FileHelper fileHelper;
  @Autowired private FacilityRepository facilityRepository;
  @Autowired private GeographicZoneRepository geographicZoneRepository;
  @Autowired private FacilityTypeRepository facilityTypeRepository;
  @Autowired private FacilityOperatorRepository facilityOperatorRepository;

  @Override
  public List<FacilityDto> processAndPersist(InputStream dataStream) {
    List<FacilityDto> importedDtos = fileHelper.readCsv(FacilityDto.class, dataStream);
    List<Facility> persistedObjects = facilityRepository.saveAll(createOrUpdate(importedDtos));

    return FacilityDto.newInstances(persistedObjects);
  }

  @Override
  public List<Facility> createOrUpdate(List<FacilityDto> dtoList) {
    final List<Facility> persistList = new LinkedList<>();
    for (FacilityDto dto : dtoList) {
      Optional<Facility> latestFacility = facilityRepository.findByCode(dto.getCode());

      if (latestFacility.isPresent()) {
        latestFacility.get().updateFrom(dto);
        persistList.add(latestFacility.get());
      } else {
        persistList.add(build(dto));
      }
    }

    return persistList;
  }

  private Facility build(FacilityDto importer) {
    final GeographicZone geographicZone =
        Optional.ofNullable(importer.getGeographicZone())
            .map(GeographicZoneSimpleDto::getCode)
            .map(geographicZoneRepository::<GeographicZone>findByCode)
            .orElseThrow(
                () -> new ValidationMessageException(GeographicZoneMessageKeys.ERROR_NOT_FOUND));
    final FacilityType facilityType =
        Optional.ofNullable(importer.getType())
            .map(FacilityTypeDto::getCode)
            .map(facilityTypeRepository::findOneByCode)
            .orElseThrow(
                () -> new ValidationMessageException(FacilityTypeMessageKeys.ERROR_NOT_FOUND));
    final FacilityOperator facilityOperator =
        Optional.ofNullable(importer.getOperator())
            .map(FacilityOperatorDto::getCode)
            .map(facilityOperatorRepository::findByCode)
            .orElseThrow(
                () -> new ValidationMessageException(FacilityOperatorMessageKeys.ERROR_NOT_FOUND));

    final Facility facility =
        Optional.ofNullable(importer.getId())
            .flatMap(facilityRepository::findById)
            .orElseGet(Facility::new);
    facility.updateFrom(importer);
    facility.setGeographicZone(geographicZone);
    facility.setType(facilityType);
    facility.setOperator(facilityOperator);
    return facility;
  }
}
