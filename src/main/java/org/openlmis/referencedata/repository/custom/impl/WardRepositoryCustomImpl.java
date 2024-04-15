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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Ward;
import org.openlmis.referencedata.repository.custom.WardRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class WardRepositoryCustomImpl implements WardRepositoryCustom {

  protected static final String FACILITY = "facility";
  protected static final String DISABLED = "disabled";
  public static final String ID = "id";

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Page<Ward> search(UUID facilityId, Boolean disabled, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    countQuery = prepareQuery(facilityId, disabled, countQuery, true, pageable);
    Long count = entityManager.createQuery(countQuery).getSingleResult();

    if (count == 0) {
      return Pagination.getPage(Collections.emptyList(), pageable, 0);
    }

    CriteriaQuery<Ward> query = builder.createQuery(Ward.class);
    query = prepareQuery(facilityId, disabled, query, false, pageable);
    Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);

    List<Ward> result = entityManager
        .createQuery(query)
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();

    return Pagination.getPage(result, pageable, count);
  }

  private <T> CriteriaQuery<T> prepareQuery(UUID facilityId, Boolean disabled,
      CriteriaQuery<T> query, boolean count, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    Root<Ward> root = query.from(Ward.class);

    Join<Ward, Facility> facilityJoin = root.join(FACILITY);

    Predicate conjunction = builder.conjunction();

    if (count) {
      CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) query;
      query = (CriteriaQuery<T>) countQuery.select(builder.count(root));
    }

    if (facilityId != null) {
      conjunction = builder.and(conjunction, builder.equal(facilityJoin.get(ID), facilityId));
    }
    conjunction = addEqualsFilter(conjunction, builder, root, DISABLED, disabled);

    query.where(conjunction);

    if (!count && pageable != null && pageable.getSort() != null) {
      query = addSortProperties(query, root, pageable);
    }

    return query;

  }

  private Predicate addEqualsFilter(Predicate predicate, CriteriaBuilder builder, Root<Ward> root,
      String filterKey, Object filterValue) {
    if (filterValue != null) {
      return builder.and(
          predicate,
          builder.equal(
              root.get(filterKey), filterValue));
    } else {
      return predicate;
    }
  }

  private <T> CriteriaQuery<T> addSortProperties(CriteriaQuery<T> query,
      Root root, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    List<Order> orders = new ArrayList<>();
    Iterator<Sort.Order> iterator = pageable.getSort().iterator();

    Sort.Order order;
    while (iterator.hasNext()) {
      order = iterator.next();
      if (order.isAscending()) {
        orders.add(builder.asc(root.get(order.getProperty())));
      } else {
        orders.add(builder.desc(root.get(order.getProperty())));
      }
    }
    return query.orderBy(orders);
  }

}
