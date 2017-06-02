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

package org.openlmis.referencedata.repository.custom.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.repository.custom.FacilityTypeApprovedProductRepositoryCustom;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class FacilityTypeApprovedProductRepositoryImpl
    implements FacilityTypeApprovedProductRepositoryCustom {

  private static final String PROGRAM = "program";
  private static final String FACILITY_TYPE = "facilityType";
  private static final String ORDERED_DISPLAY_VALUE = "orderedDisplayValue";
  private static final String CODE = "code";
  private static final String ID = "id";

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Collection<FacilityTypeApprovedProduct> searchProducts(UUID facilityId, UUID programId,
                                                                boolean fullSupply) {
    checkNotNull(facilityId);

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<FacilityTypeApprovedProduct> query = builder.createQuery(
        FacilityTypeApprovedProduct.class
    );

    Root<FacilityTypeApprovedProduct> ftap = query.from(FacilityTypeApprovedProduct.class);
    Root<Facility> facility = query.from(Facility.class);

    Join<Facility, FacilityType> fft = facility.join("type");
    Join<FacilityTypeApprovedProduct, FacilityType> ft = ftap.join(FACILITY_TYPE);
    Join<FacilityTypeApprovedProduct, Program> program = ftap.join(PROGRAM);

    Predicate conjunctionPredicate = builder.conjunction();
    if (programId != null) {
      conjunctionPredicate = builder.and(conjunctionPredicate,
          builder.equal(program.get(ID), programId));
    }
    conjunctionPredicate = builder.and(conjunctionPredicate,
        builder.equal(fft.get(ID), ft.get(ID)));
    conjunctionPredicate = builder.and(conjunctionPredicate,
        builder.equal(facility.get(ID), facilityId));

    Join<FacilityTypeApprovedProduct, Orderable> orderable = ftap.join("orderable");
    Join<Orderable, Set<ProgramOrderable>> programOrderables =
        orderable.joinSet("programOrderables");

    conjunctionPredicate = builder.and(conjunctionPredicate,
        builder.equal(programOrderables.get("fullSupply"), fullSupply));
    conjunctionPredicate = builder.and(conjunctionPredicate,
        builder.isTrue(programOrderables.get("active")));
    conjunctionPredicate = builder.and(conjunctionPredicate,
        builder.equal(programOrderables.get(PROGRAM), program));

    query.select(ftap);
    query.where(conjunctionPredicate);

    Join<ProgramOrderable, OrderableDisplayCategory> category =
        programOrderables.join("orderableDisplayCategory");

    query.orderBy(
        builder.asc(category.get(ORDERED_DISPLAY_VALUE).get("displayOrder")),
        builder.asc(category.get(ORDERED_DISPLAY_VALUE).get("displayName")),
        builder.asc(orderable.get("productCode"))
    );

    return entityManager.createQuery(query).getResultList();
  }

  @Override
  public Collection<FacilityTypeApprovedProduct> searchProducts(String facilityTypeCode,
                                                                String programCode) {
    checkNotNull(facilityTypeCode);

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<FacilityTypeApprovedProduct> query = builder.createQuery(
        FacilityTypeApprovedProduct.class
    );

    Root<FacilityTypeApprovedProduct> ftap = query.from(FacilityTypeApprovedProduct.class);

    Join<FacilityTypeApprovedProduct, FacilityType> ft = ftap.join(FACILITY_TYPE);
    Join<FacilityTypeApprovedProduct, Program> program = ftap.join(PROGRAM);

    Predicate conjunctionPredicate = builder.conjunction();
    if (StringUtils.isNotBlank(programCode)) {
      conjunctionPredicate = builder.and(conjunctionPredicate,
          builder.equal(program.get(CODE), Code.code(programCode)));
    }
    conjunctionPredicate = builder.and(conjunctionPredicate,
        builder.equal(ft.get(CODE), facilityTypeCode));
    Join<FacilityTypeApprovedProduct, Orderable> orderable = ftap.join("orderable");
    Join<Orderable, Set<ProgramOrderable>> programOrderables =
        orderable.joinSet("programOrderables");

    conjunctionPredicate = builder.and(conjunctionPredicate,
        builder.isTrue(programOrderables.get("active")));
    conjunctionPredicate = builder.and(conjunctionPredicate,
        builder.equal(programOrderables.get(PROGRAM), program));

    query.select(ftap);
    query.where(conjunctionPredicate);

    Join<ProgramOrderable, OrderableDisplayCategory> category =
        programOrderables.join("orderableDisplayCategory");

    query.orderBy(
        builder.asc(category.get(ORDERED_DISPLAY_VALUE).get("displayOrder")),
        builder.asc(category.get(ORDERED_DISPLAY_VALUE).get("displayName")),
        builder.asc(orderable.get("productCode"))
    );

    return entityManager.createQuery(query).getResultList();
  }
}
