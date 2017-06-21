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

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.repository.custom.RequisitionGroupProgramScheduleRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
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
   *
   * @param program  Program of searched RequisitionGroupProgramSchedule
   * @param facility Facility of searched RequisitionGroupProgramSchedule
   * @return Requisition Group Program Schedule matching search criteria
   */
  public List<RequisitionGroupProgramSchedule> searchRequisitionGroupProgramSchedule(
      Program program, Facility facility) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<RequisitionGroupProgramSchedule> query = builder.createQuery(
        RequisitionGroupProgramSchedule.class
    );

    Root<RequisitionGroupProgramSchedule> rgps = query.from(RequisitionGroupProgramSchedule.class);

    Join<RequisitionGroupProgramSchedule, RequisitionGroup> rg = rgps.join("requisitionGroup");
    Join<RequisitionGroup, Facility> ft = rg.join("memberFacilities");

    Predicate conjunction = builder.conjunction();
    if (facility != null) {
      conjunction = builder.and(conjunction, builder.equal(ft.get("id"), facility.getId()));
    }
    if (program != null) {
      conjunction = builder.and(conjunction, builder.equal(rgps.get("program"), program));
    }

    query.select(rgps);
    query.where(conjunction);

    try {
      return entityManager.createQuery(query).getResultList();
    } catch (NoResultException exp) {
      return null;
    }
  }
}
