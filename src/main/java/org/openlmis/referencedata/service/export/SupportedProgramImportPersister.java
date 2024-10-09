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
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.domain.SupportedProgramPrimaryKey;
import org.openlmis.referencedata.dto.SupportedProgramCsvModel;
import org.openlmis.referencedata.dto.SupportedProgramDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupportedProgramRepository;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("supportedProgram.csv")
public class SupportedProgramImportPersister
    implements DataImportPersister<
        SupportedProgram, SupportedProgramCsvModel, SupportedProgramDto> {

  @Autowired private FileHelper fileHelper;
  @Autowired private SupportedProgramRepository supportedProgramRepository;
  @Autowired private FacilityRepository facilityRepository;
  @Autowired private ProgramRepository programRepository;

  @Override
  public List<SupportedProgramDto> processAndPersist(InputStream dataStream) {
    List<SupportedProgramCsvModel> importedDtos =
        fileHelper.readCsv(SupportedProgramCsvModel.class, dataStream);
    List<SupportedProgram> persistedObjects =
        supportedProgramRepository.saveAll(createOrUpdate(importedDtos));

    return SupportedProgramDto.newInstances(persistedObjects);
  }

  @Override
  public List<SupportedProgram> createOrUpdate(List<SupportedProgramCsvModel> dtoList) {
    final List<SupportedProgram> persistList = new LinkedList<>();
    for (SupportedProgramCsvModel dto : dtoList) {
      final Facility facility =
          facilityRepository
              .findByCode(dto.getFacilityCode())
              .orElseThrow(
                  () -> new ValidationMessageException(FacilityMessageKeys.ERROR_NOT_FOUND));
      final Program program =
          Optional.ofNullable(
                  programRepository.<Program>findByCode(Code.code(dto.getProgramCode())))
              .orElseThrow(
                  () -> new ValidationMessageException(ProgramMessageKeys.ERROR_NOT_FOUND));

      Optional<SupportedProgram> latestSupportedProgram =
          supportedProgramRepository.findById(new SupportedProgramPrimaryKey(facility, program));

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
}
