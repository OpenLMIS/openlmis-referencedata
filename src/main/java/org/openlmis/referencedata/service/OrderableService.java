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

import java.util.Set;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys;
import org.openlmis.referencedata.web.OrderableSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderableService {

  @Autowired
  private OrderableRepository orderableRepository;

  /**
   * Method returns all orderables with matched parameters.
   *
   * @param queryMap request parameters (code, name, description, program).
   * @param pageable the page to get, or one page with all if null.
   * @return the Page of orderables found, or an empty page.
   */
  public Page<Orderable> searchOrderables(@NotNull OrderableSearchParams queryMap,
      Pageable pageable) {
    if (!queryMap.isValid()) {
      throw new ValidationMessageException(
          OrderableMessageKeys.ERROR_INVALID_PARAMS);
    }
    if (queryMap.isEmpty()) {
      return orderableRepository.findAll(pageable);
    }

    Set<UUID> ids = queryMap.getIds();
    if (!ids.isEmpty()) {
      return orderableRepository.findAllByIds(ids, pageable);
    }

    String code = queryMap.getCode();
    String name = queryMap.getName();
    Code programCode = queryMap.getProgramCode();

    return orderableRepository.search(code, name, programCode, pageable);
  }
}
