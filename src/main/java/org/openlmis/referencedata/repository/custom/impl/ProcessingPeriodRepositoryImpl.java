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

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.repository.custom.ProcessingPeriodRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class ProcessingPeriodRepositoryImpl implements ProcessingPeriodRepositoryCustom {

  private static final String PROCESSING_SCHEDULE = "processingSchedule";
  private static final String START_DATE = "startDate";
  private static final String END_DATE = "endDate";
  private static final String ID = "id";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all Processing Periods with matched parameters.
   * Method is searching
   *
   * @param schedule  Processing Schedule associated to Processing Period
   * @param startDate Processing Period Start Date
   * @param endDate   Processing Period End Date
   * @param pageable  pagination and sorting parameters
   * @return Page of Processing Periods matching the parameters.
   */
  public Page<ProcessingPeriod> search(ProcessingSchedule schedule, LocalDate startDate,
      LocalDate endDate, Collection<UUID> ids, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<ProcessingPeriod> periodQuery = builder.createQuery(ProcessingPeriod.class);
    periodQuery =
        prepareQuery(periodQuery, schedule, startDate, endDate, ids, false, builder, pageable);

    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    countQuery =
        prepareQuery(countQuery, schedule, startDate, endDate, ids, true, builder, pageable);

    Long count = entityManager.createQuery(countQuery).getSingleResult();

    Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);

    List<ProcessingPeriod> processingPeriods = entityManager.createQuery(periodQuery)
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();
    return Pagination.getPage(processingPeriods, pageable, count);
  }

  private <T> CriteriaQuery<T> prepareQuery(CriteriaQuery<T> query, ProcessingSchedule schedule,
      LocalDate startDate, LocalDate endDate, Collection<UUID> ids, boolean count,
      CriteriaBuilder builder, Pageable pageable) {
    Root<ProcessingPeriod> root = query.from(ProcessingPeriod.class);

    if (count) {
      CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) query;
      query = (CriteriaQuery<T>) countQuery.select(builder.count(root));
    }

    Predicate predicate = builder.conjunction();

    if (null != schedule) {
      predicate = builder.and(predicate, builder.equal(root.get(PROCESSING_SCHEDULE), schedule));
    }

    if (null != startDate) {
      predicate = builder.and(predicate, builder
          .greaterThanOrEqualTo(root.get(END_DATE), startDate));
    }

    if (null != endDate) {
      predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get(START_DATE), endDate));
    }

    if (!isEmpty(ids)) {
      predicate = builder.and(predicate, root.get(ID).in(ids));
    }

    query.where(predicate);

    if (!count && pageable != null && pageable.getSort() != null) {
      query = addSortProperties(query, root, builder, pageable);
    }

    return query;
  }

  private <T> CriteriaQuery<T> addSortProperties(CriteriaQuery<T> query,
      Root<ProcessingPeriod> root, CriteriaBuilder builder, Pageable pageable) {
    List<Order> orders = new ArrayList<>();
    Iterator<Sort.Order> iterator = pageable.getSort().iterator();
    Sort.Order order;

    while (iterator.hasNext()) {
      order = iterator.next();
      String property = order.getProperty();
      Path path = root.get(property);

      if (order.isAscending()) {
        orders.add(builder.asc(path));
      } else {
        orders.add(builder.desc(path));
      }
    }

    return query.orderBy(orders);
  }
}
