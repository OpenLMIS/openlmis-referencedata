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
import java.util.LinkedList;
import java.util.List;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.dto.ProgramOrderableCsvModel;
import org.openlmis.referencedata.dto.ProgramOrderableDto;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramOrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.util.FileHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("programOrderable.csv")
public class ProgramOrderableImportPersister implements DataImportPersister<ProgramOrderable,
    ProgramOrderableCsvModel, ProgramOrderableDto> {

  @Autowired
  private FileHelper fileHelper;

  @Autowired
  private ProgramOrderableRepository programOrderableRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  @Override
  public List<ProgramOrderableDto> processAndPersist(InputStream dataStream) {
    List<ProgramOrderableCsvModel> importedDtos =
        fileHelper.readCsv(ProgramOrderableCsvModel.class, dataStream);
    List<ProgramOrderable> persistedObjects = programOrderableRepository.saveAll(
        createOrUpdate(importedDtos)
    );

    return new ArrayList<>(ProgramOrderableDto.newInstance(persistedObjects));
  }

  @Override
  public List<ProgramOrderable> createOrUpdate(List<ProgramOrderableCsvModel> dtoList) {
    List<ProgramOrderable> persistList = new LinkedList<>();

    for (ProgramOrderableCsvModel dto: dtoList) {
      Program program = programRepository.findByCode(Code.code(dto.getProgramCode()));
      Orderable orderable = orderableRepository
          .findFirstByProductCodeOrderByIdentityVersionNumberDesc(
              Code.code(dto.getOrderableCode()));
      OrderableDisplayCategory orderableDisplayCategory = orderableDisplayCategoryRepository
          .findByCode(Code.code(dto.getCategoryCode()));

      CurrencyUnit currency = CurrencyUnit.of(System.getenv("CURRENCY_CODE"));

      ProgramOrderableDto programOrderableDto = new ProgramOrderableDto(
          program.getId(),
          orderableDisplayCategory.getId(),
          orderableDisplayCategory.getOrderedDisplayValue().getDisplayName(),
          orderableDisplayCategory.getOrderedDisplayValue().getDisplayOrder(),
          dto.isActive(),
          dto.isFullSupply(),
          dto.getDisplayOrder(),
          dto.getDosesPerPatient(),
          dto.getPricePerPack() != null ? Money.of(currency,
              Double.parseDouble(dto.getPricePerPack())) : null,
          null
      );

      ProgramOrderable programOrderable = programOrderableRepository
          .findByProgramCodeOrderableCodeCategoryCode(dto.getProgramCode(),
              dto.getOrderableCode(), dto.getCategoryCode()
          );

      if (programOrderable == null) {
        programOrderable = ProgramOrderable.createNew(program, orderableDisplayCategory,
            orderable, currency);
      }

      programOrderable.updateFrom(programOrderableDto);
      persistList.add(programOrderable);
    }

    return persistList;
  }

}
