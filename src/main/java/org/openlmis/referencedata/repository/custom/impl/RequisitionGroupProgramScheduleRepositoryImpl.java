package org.openlmis.referencedata.repository.custom.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.exception.RequisitionGroupProgramScheduleException;
import org.openlmis.referencedata.repository.custom.RequisitionGroupProgramScheduleRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;


public class RequisitionGroupProgramScheduleRepositoryImpl implements
    RequisitionGroupProgramScheduleRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Retrieves requisition group program schedule from reference data service
   * by program and facility.
   *
   * @param program  Program of searched RequisitionGroupProgramSchedule
   * @param facility Facility of searched RequisitionGroupProgramSchedule
   * @return Requisition Group Program Schedule matching search criteria
   */
  public RequisitionGroupProgramSchedule searchRequisitionGroupProgramSchedule(
      Program program, Facility facility) throws RequisitionGroupProgramScheduleException {
    checkNotNull(program);
    checkNotNull(facility);

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<RequisitionGroupProgramSchedule> query = builder.createQuery(
        RequisitionGroupProgramSchedule.class
    );

    Root<RequisitionGroupProgramSchedule> rgps = query.from(RequisitionGroupProgramSchedule.class);

    Join<RequisitionGroupProgramSchedule, RequisitionGroup> rg = rgps.join("requisitionGroup");
    Join<RequisitionGroup, Facility> ft = rg.join("memberFacilities");

    Predicate conjunction = builder.conjunction();
    conjunction = builder.and(conjunction, builder.equal(ft.get("id"), facility.getId()));
    conjunction = builder.and(conjunction, builder.equal(rgps.get("program"), program));

    query.select(rgps);
    query.where(conjunction);

    try {
      return entityManager.createQuery(query).getSingleResult();
    } catch (NoResultException exp) {
      return null;
    }
  }
}
