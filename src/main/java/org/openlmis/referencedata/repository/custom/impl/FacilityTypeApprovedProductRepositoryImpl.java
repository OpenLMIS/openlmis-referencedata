package org.openlmis.referencedata.repository.custom.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.repository.custom.FacilityTypeApprovedProductRepositoryCustom;

import java.util.Collection;
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

    Join<FacilityTypeApprovedProduct, FacilityType> ft = ftap.join("facilityType");
    Join<FacilityTypeApprovedProduct, ProgramOrderable> pp = ftap.join("programOrderable");

    Join<ProgramOrderable, Program> program = pp.join("program");

    Predicate conjunction = builder.conjunction();
    if (programId != null) {
      conjunction = builder.and(conjunction, builder.equal(program.get("id"), programId));
    }
    conjunction = builder.and(conjunction, builder.equal(fft.get("id"), ft.get("id")));
    conjunction = builder.and(conjunction, builder.equal(facility.get("id"), facilityId));
    conjunction = builder.and(conjunction, builder.equal(pp.get("fullSupply"), fullSupply));
    conjunction = builder.and(conjunction, builder.isTrue(pp.get("active")));

    query.select(ftap);
    query.where(conjunction);

    Join<ProgramOrderable, OrderableDisplayCategory> category = pp.join("orderableDisplayCategory");
    Join<ProgramOrderable, Orderable> product = pp.join("product");

    query.orderBy(
        builder.asc(category.get("orderedDisplayValue").get("displayOrder")),
        builder.asc(category.get("orderedDisplayValue").get("displayName")),
        builder.asc(product.get("productCode"))
    );

    return entityManager.createQuery(query).getResultList();
  }

}
