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

import java.util.UUID;
import java.util.function.Function;
import org.openlmis.referencedata.domain.BaseEntity.BaseImporter;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.util.messagekeys.FacilityTypeMessageKeys;
import org.openlmis.referencedata.util.messagekeys.OrderableMessageKeys;
import org.openlmis.referencedata.util.messagekeys.ProgramMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FacilityTypeApprovedProductBuilder {

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  /**
   * Creates new {@link FacilityTypeApprovedProduct} based on data from importer.
   */
  public FacilityTypeApprovedProduct build(FacilityTypeApprovedProduct.Importer importer) {
    Orderable orderable = findResource(orderableRepository::findOne, importer.getOrderable(),
        OrderableMessageKeys.ERROR_NOT_FOUND);
    Program program = findResource(programRepository::findOne, importer.getProgram(),
        ProgramMessageKeys.ERROR_NOT_FOUND);
    FacilityType facilityType = findResource(facilityTypeRepository::findOne,
        importer.getFacilityType(), FacilityTypeMessageKeys.ERROR_NOT_FOUND);

    FacilityTypeApprovedProduct approvedProduct = FacilityTypeApprovedProduct
        .newFacilityTypeApprovedProduct(importer);
    approvedProduct.setOrderable(orderable);
    approvedProduct.setProgram(program);
    approvedProduct.setFacilityType(facilityType);

    return approvedProduct;
  }

  private <R> R findResource(Function<UUID, R> finder, BaseImporter importer, String errorMessage) {
    if (null == importer || null == importer.getId()) {
      throw new ValidationMessageException(errorMessage);
    }

    R resource = finder.apply(importer.getId());

    if (null == resource) {
      throw new ValidationMessageException(errorMessage);
    }

    return resource;
  }


}
