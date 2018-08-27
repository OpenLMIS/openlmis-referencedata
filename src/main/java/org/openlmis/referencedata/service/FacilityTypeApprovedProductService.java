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

import static org.openlmis.referencedata.util.messagekeys.FacilityTypeApprovedProductMessageKeys.ERROR_LACK_PARAMS;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityTypeApprovedProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FacilityTypeApprovedProductService {

  private static final String FACILITY_TYPE = "facilityType";
  private static final String PROGRAM = "program";

  @Autowired
  private FacilityTypeApprovedProductRepository repository;

  /**
   * Verifies that the required parameters are present in the query map and forwards the
   * query to the database.
   *
   * @param queryParams query parameters for the request
   * @return a page of approved products that match the search criteria
   */
  public Page<FacilityTypeApprovedProduct> search(Map<String, String> queryParams,
                                                  Pageable pageable) {
    String facilityTypeCode = queryParams.get(FACILITY_TYPE);
    String programCode = queryParams.get(PROGRAM);

    if (StringUtils.isBlank(facilityTypeCode)) {
      throw new ValidationMessageException(ERROR_LACK_PARAMS);
    }

    return repository.searchProducts(facilityTypeCode, programCode, pageable);
  }
}
