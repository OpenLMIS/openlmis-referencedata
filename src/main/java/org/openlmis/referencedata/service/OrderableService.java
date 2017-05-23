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

import com.google.common.collect.Lists;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderableService {

  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String PROGRAM_CODE = "program";

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private ProgramRepository programRepository;

  /**
   * Method returns all orderables with matched parameters.
   *
   * @param queryMap request parameters (code, name, description, program).
   * @return List of orderables
   */
  public List<Orderable> searchOrderables(Map<String, Object> queryMap) {

    if (MapUtils.isEmpty(queryMap)) {
      return Lists.newArrayList(orderableRepository.findAll());
    }

    String code = MapUtils.getString(queryMap, CODE, null);
    String name = MapUtils.getString(queryMap, NAME, null);
    String programCode = MapUtils.getString(queryMap, PROGRAM_CODE, null);

    // validate query parameters
    if (StringUtils.isEmpty(code)
            && StringUtils.isEmpty(name)
            && StringUtils.isEmpty(programCode)) {

      throw new ValidationMessageException(
              OrderableMessageKeys.ERROR_SEARCH_LACKS_PARAMS);
    }

    // find facility type if given
    Program program = null;
    if (programCode != null) {
      program = programRepository.findByCode(Code.code(programCode));
      if (program == null) {
        throw new ValidationMessageException(ProgramMessageKeys.ERROR_NOT_FOUND);
      }
    }

    List<Orderable> foundOrderables = orderableRepository.search(code, name, program);

    return Optional.ofNullable(foundOrderables).orElse(Collections.emptyList());
  }
}
