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

package org.openlmis.referencedata.util;

import com.google.common.collect.Maps;

import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderableBuilder {

  @Autowired
  private ProgramRepository programRepository;

  /**
   * Creates new instance based on data from {@link Orderable.Importer}
   *
   * @param importer instance of {@link Orderable.Importer}
   * @return new instance of Orderable.
   */
  public Orderable newOrderable(Orderable.Importer importer) {
    Map<UUID, Program> programs;

    if (importer.getPrograms() != null) {
      programs = importer
          .getPrograms()
          .stream()
          .map(item -> programRepository.findOne(item.getProgramId()))
          .collect(Collectors.toMap(Program::getId, program -> program));
    } else {
      programs = Maps.newHashMap();
    }

    return Orderable.newInstance(importer, programs);
  }

}
