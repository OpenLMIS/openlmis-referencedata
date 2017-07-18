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

import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.repository.custom.RequisitionGroupRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class RequisitionGroupRepositoryImpl implements RequisitionGroupRepositoryCustom {

  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String PROGRAM = "program";
  private static final String PROGRAM_SCHEDULES = "requisitionGroupProgramSchedules";
  private static final String SUPERVISORY_NODE = "supervisoryNode";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all facilities with matched parameters.
   * Method is ignoring case for facility code and name.
   * To find all wanted Facilities by conde and name we use criteria query and like operator.
   *
   * @param code Part of wanted code.
   * @param name Part of wanted name.
   * @param program Geographic zone of facility location.
   * @param supervisoryNodes Wanted facility type.
   * @return List of Facilities matching the parameters.
   */
  public Page<RequisitionGroup> search(String code, String name,
                                       Program program, List<SupervisoryNode> supervisoryNodes,
                                       Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<RequisitionGroup> query = builder.createQuery(RequisitionGroup.class);
    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);

    query = prepareQuery(query, code, name, program, supervisoryNodes, false);
    countQuery = prepareQuery(countQuery, code, name, program, supervisoryNodes, true);

    Long count = entityManager.createQuery(countQuery).getSingleResult();

    List<RequisitionGroup> result = entityManager.createQuery(query)
        .setMaxResults(pageable.getPageSize())
        .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
        .getResultList();

    return Pagination.getPage(result, pageable, count);
  }

  private <T> CriteriaQuery<T> prepareQuery(CriteriaQuery<T> query, String code,
                                            String name, Program program,
                                            List<SupervisoryNode> supervisoryNodes,
                                            boolean count) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    Root<RequisitionGroup> root = query.from(RequisitionGroup.class);

    if (count) {
      CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) query;
      query = (CriteriaQuery<T>) countQuery.select(builder.count(root));
    }

    Predicate predicate = builder.conjunction();

    if (code != null) {
      predicate = builder.and(predicate,
          builder.like(builder.upper(root.get(CODE)), "%" + code.toUpperCase() + "%"));
    }

    if (name != null) {
      predicate = builder.and(predicate,
          builder.like(builder.upper(root.get(NAME)), "%" + name.toUpperCase() + "%"));
    }

    if (program != null) {
      Join<RequisitionGroup, RequisitionGroupProgramSchedule> programSchedulesJoin =
          root.join(PROGRAM_SCHEDULES, JoinType.LEFT);
      predicate = builder.and(predicate,
          builder.equal(programSchedulesJoin.get(PROGRAM), program));
    }

    if (supervisoryNodes != null && !supervisoryNodes.isEmpty()) {
      Predicate supervisoryNodePredicate = builder.disjunction();
      for (SupervisoryNode node : supervisoryNodes) {
        supervisoryNodePredicate = builder.or(supervisoryNodePredicate,
            builder.equal(root.get(SUPERVISORY_NODE), node));
      }
      predicate = builder.and(predicate, supervisoryNodePredicate);
    }

    query.where(predicate);

    if (!count) {
      query.orderBy(builder.asc(root.get(NAME)));
    }

    return query;
  }
}
