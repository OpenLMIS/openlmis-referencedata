package org.openlmis.referencedata.repository.custom.impl;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.exception.RequisitionGroupProgramScheduleException;
import org.openlmis.referencedata.repository.custom.RequisitionGroupProgramScheduleRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;


public class RequisitionGroupProgramScheduleRepositoryImpl implements
      RequisitionGroupProgramScheduleRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Retrieves requisition group program schedule from reference data service
   * by program and facility.
   * @param program Program of searched RequisitionGroupProgramSchedule
   * @param facility Facility of searched RequisitionGroupProgramSchedule
   * @return Requisition Group Program Schedule matching search criteria
   */
  public RequisitionGroupProgramSchedule searchRequisitionGroupProgramSchedule(
        Program program, Facility facility) throws RequisitionGroupProgramScheduleException {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<RequisitionGroupProgramSchedule> query =
          builder.createQuery(RequisitionGroupProgramSchedule.class);
    Root<RequisitionGroupProgramSchedule> root = query.from(RequisitionGroupProgramSchedule.class);
    Predicate predicate = builder.conjunction();
    if (program != null) {
      predicate = builder.and(
            predicate,
            builder.equal(
                  root.get("program"), program));
    }
    if (facility != null) {
      predicate = builder.and(
            predicate,
            builder.equal(
                  root.get("dropOffFacility"), facility));
    }
    query.where(predicate);

    List<RequisitionGroupProgramSchedule> requisitionGroupProgramSchedules
          = entityManager.createQuery(query).getResultList();
    if (requisitionGroupProgramSchedules == null || requisitionGroupProgramSchedules.isEmpty()) {
      return null;
    } else if (requisitionGroupProgramSchedules.size() != 1) {
      throw new RequisitionGroupProgramScheduleException(
            "There cannot exists more than one requisition group program schedule"
                  + " for program and facility");
    } else {
      return entityManager.createQuery(query).getSingleResult();
    }
  }
}
