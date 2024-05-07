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

package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.UnitOfOrderable;
import org.openlmis.referencedata.dto.UnitOfOrderableDto;
import org.openlmis.referencedata.repository.UnitOfOrderableRepository;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UnitOfOrderableController.RESOURCE_PATH)
public class UnitOfOrderableController extends BaseController {
  public static final String RESOURCE_PATH = BaseController.API_PATH + "/unitOfOrderables";

  @Autowired private UnitOfOrderableRepository unitOfOrderableRepository;

  /**
   * REST endpoint to get paginated UnitOfOrderable.
   *
   * @param pageable the requested page details, not null
   * @return a list of Unit Of Orderable Dtos, never null
   */
  @GetMapping
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  public Page<UnitOfOrderableDto> getUnits(Pageable pageable) {
    final Page<UnitOfOrderable> unitsPage = unitOfOrderableRepository.findAll(pageable);
    return Pagination.getPage(
        UnitOfOrderableDto.newInstances(unitsPage.toList()),
        pageable,
        unitsPage.getTotalElements());
  }
}
