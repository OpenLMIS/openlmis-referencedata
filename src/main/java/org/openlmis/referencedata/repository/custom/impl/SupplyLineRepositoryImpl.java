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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.repository.custom.SupplyLineRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class SupplyLineRepositoryImpl implements SupplyLineRepositoryCustom {

  private static final String SUPPLYING_FACILITY = "supplyingFacility";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Method returns all Supply lines with matched parameters.
   *
   * @param program           program of searched Supply Lines.
   * @param supervisoryNode   supervisoryNode of searched Supply Lines.
   * @param supplyingFacility supplyingFacility of searched Supply Lines.
   * @return list of Supply Lines with matched parameters.
   */
  @Override
  public List<SupplyLine> searchSupplyLines(Program program, SupervisoryNode supervisoryNode,
                                            Facility supplyingFacility) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<SupplyLine> query = builder.createQuery(SupplyLine.class);

    query = prepareSearchQuery(query, program, supervisoryNode, supplyingFacility, null, false);

    return entityManager.createQuery(query).getResultList();
  }

  /**
   * Method returns page of Supply lines with matched parameters.
   * Result can be sorted by supplying facility name if
   * "supplyingFacilityName" parameter is used in sort property in pageable object.
   *
   * @param program           program of searched Supply Lines.
   * @param supervisoryNode   supervisoryNode of searched Supply Lines.
   * @param supplyingFacility supplyingFacility of searched Supply Lines.
   * @param pageable          object with pagination and sorting parameters
   * @return page of Supply Lines with matched parameters.
   */
  @Override
  public Page<SupplyLine> searchSupplyLines(Program program, SupervisoryNode supervisoryNode,
                                            Facility supplyingFacility, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<SupplyLine> query = builder.createQuery(SupplyLine.class);
    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);

    query = prepareSearchQuery(query, program, supervisoryNode,
        supplyingFacility, pageable, false);
    countQuery = prepareSearchQuery(countQuery, program, supervisoryNode,
        supplyingFacility, pageable, true);

    Long count = entityManager.createQuery(countQuery).getSingleResult();

    List<SupplyLine> result = entityManager.createQuery(query)
        .setMaxResults(pageable.getPageSize())
        .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
        .getResultList();

    return Pagination.getPage(result, pageable, count);
  }

  private <T> CriteriaQuery<T> prepareSearchQuery(CriteriaQuery<T> query,
                                                  Program program,
                                                  SupervisoryNode supervisoryNode,
                                                  Facility supplyingFacility,
                                                  Pageable pageable,
                                                  boolean count) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    Root<SupplyLine> root = query.from(SupplyLine.class);

    if (count) {
      CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) query;
      query = (CriteriaQuery<T>) countQuery.select(builder.count(root));
    }

    Predicate predicate = builder.conjunction();

    if (program != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("program"), program));
    }

    if (supervisoryNode != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("supervisoryNode"), supervisoryNode));
    }

    if (supplyingFacility != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("supplyingFacility"), supplyingFacility));
    }

    query.where(predicate);

    if (!count && pageable != null && pageable.getSort() != null) {
      query = addSortProperties(query, root, pageable);
    }

    return query;
  }

  private <T> CriteriaQuery<T> addSortProperties(CriteriaQuery<T> query,
                                                 Root root, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    List<Order> orders = new ArrayList<>();
    Iterator<Sort.Order> iterator = pageable.getSort().iterator();
    Sort.Order order;

    while (iterator.hasNext()) {
      order = iterator.next();
      String property = order.getProperty();

      Path path;
      if (SUPPLYING_FACILITY.equals(property)) {
        path = root.join(SUPPLYING_FACILITY, JoinType.LEFT).get("name");
      } else {
        path = root.get(property);
      }
      if (order.isAscending()) {
        orders.add(builder.asc(path));
      } else {
        orders.add(builder.desc(path));
      }
    }
    return query.orderBy(orders);
  }
}
