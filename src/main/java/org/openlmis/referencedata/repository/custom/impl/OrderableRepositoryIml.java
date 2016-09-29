package org.openlmis.referencedata.repository.custom.impl;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramProduct;
import org.openlmis.referencedata.repository.custom.OrderableProductRepositoryCustom;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class OrderableRepositoryIml implements OrderableProductRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;


  @Override
  public List<OrderableProduct> searchApprovedOrderableProductsByProgramAndFacility(
      Program program, Facility facility) {

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<OrderableProduct> criteriaQuery = builder.createQuery(OrderableProduct.class);

    //Root<Facility> facilityRoot = criteriaQuery.from(Facility.class);
    //Root<Program> programRoot = criteriaQuery.from(Program.class);

    Root<FacilityTypeApprovedProduct> facilityTypeApprovedProductRoot =
        criteriaQuery.from(FacilityTypeApprovedProduct.class);
    Path<FacilityType> facilityTypePath = facilityTypeApprovedProductRoot.get("facilityType");
    Path<ProgramProduct> programProductPath =
        facilityTypeApprovedProductRoot.get("programProduct");

    
    Predicate predicate = builder.conjunction();
    predicate = builder.and(predicate, builder.equal(facilityTypePath, facility.getType()));

    Root<ProgramProduct> programProductRoot = criteriaQuery.from(ProgramProduct.class);
    predicate = builder.and(predicate, builder.equal(programProductRoot, programProductPath));
    Path<Program> programPath = programProductRoot.get("program");
    predicate = builder.and(predicate, builder.equal(programPath, program));

    Path<OrderableProduct> orderableProductPath = programProductRoot.get("product");
    Root<OrderableProduct> orderableProductRoot = criteriaQuery.from(OrderableProduct.class);
    predicate = builder.and(predicate, builder.equal(orderableProductRoot, orderableProductPath));

    predicate = builder.and(predicate, builder.equal(
        programProductRoot.get("fullSupply"), Boolean.TRUE));
    predicate = builder.and(predicate, builder.equal(
        programProductRoot.get("active"), Boolean.TRUE));


    criteriaQuery = criteriaQuery.where(predicate);
    Query query = entityManager.createQuery(criteriaQuery);

    return query.getResultList();
  }
}
