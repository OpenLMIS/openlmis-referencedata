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

package org.openlmis.referencedata.service;

import static java.util.stream.Collectors.toList;

import java.util.List;
import org.openlmis.referencedata.dto.SupportedProgramCsvModel;
import org.openlmis.referencedata.repository.SupportedProgramRepository;
import org.openlmis.referencedata.service.export.ExportableDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupportedProgramService implements ExportableDataService<SupportedProgramCsvModel> {
  @Autowired private SupportedProgramRepository supportedProgramRepository;

  @Override
  public List<SupportedProgramCsvModel> findAllExportableItems() {
    return supportedProgramRepository.findAll().stream()
        .map(SupportedProgramCsvModel::newInstance)
        .collect(toList());
  }

  @Override
  public Class<SupportedProgramCsvModel> getExportableType() {
    return SupportedProgramCsvModel.class;
  }
}
