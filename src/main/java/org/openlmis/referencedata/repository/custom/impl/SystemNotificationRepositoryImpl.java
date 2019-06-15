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

import com.google.common.base.Preconditions;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.referencedata.domain.SystemNotification;
import org.openlmis.referencedata.repository.custom.SystemNotificationRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class SystemNotificationRepositoryImpl implements SystemNotificationRepositoryCustom {

  private static final String ID = "id";
  private static final String AUTHOR = "author";
  private static final String ACTIVE = "active";
  private static final String EXPIRY_DATE = "expiryDate";
  private static final String START_DATE = "startDate";
  private static final ZonedDateTime NOW = ZonedDateTime.now();

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all system notifications with matched parameters.
   */
  public Page<SystemNotification> search(
      SystemNotificationRepositoryCustom.SearchParams params, Pageable pageable) {
    Preconditions.checkNotNull(params);
    Preconditions.checkNotNull(pageable);

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    countQuery = prepareQuery(builder, countQuery, true, params);

    Long count = entityManager.createQuery(countQuery).getSingleResult();

    if (count == 0) {
      return Pagination.getPage(Collections.emptyList());
    }

    CriteriaQuery<SystemNotification> query = builder.createQuery(SystemNotification.class);
    query = prepareQuery(builder, query, false, params);

    Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);

    List<SystemNotification> resultList = entityManager.createQuery(query)
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();

    return Pagination.getPage(resultList, pageable, count);
  }

  private <E> CriteriaQuery<E> prepareQuery(CriteriaBuilder builder, CriteriaQuery<E> query,
      boolean count, SystemNotificationRepositoryCustom.SearchParams params) {

    Root<SystemNotification> root = query.from(SystemNotification.class);
    CriteriaQuery<E> newQuery;

    if (count) {
      CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) query;
      newQuery = (CriteriaQuery<E>) countQuery.select(builder.count(root));
    } else {
      CriteriaQuery<SystemNotification> typeQuery = (CriteriaQuery<SystemNotification>) query;
      newQuery = (CriteriaQuery<E>) typeQuery.select(root);
    }

    Predicate where = builder.conjunction();
    UUID authorId = params.getAuthorId();
    Boolean isDisplayed = params.getIsDisplayed();
    Path<ZonedDateTime> expiryDate = root.get(EXPIRY_DATE);
    Path<ZonedDateTime> startDate = root.get(START_DATE);
    Path<Boolean> active = root.get(ACTIVE);

    if (authorId != null) {
      where = builder.and(where, builder.equal(root.get(AUTHOR).get(ID), authorId));
    }

    if (isDisplayed != null) {
      where = builder.and(where,
          preparePredicate(isDisplayed, startDate, expiryDate, builder, active));
    }

    if (!count) {
      newQuery.orderBy(
          builder.desc(active),
          builder.desc(expiryDate));
    }

    newQuery.where(where);

    return newQuery;
  }

  private Predicate preparePredicate(Boolean isDisplayed, Path<ZonedDateTime> startDate,
      Path<ZonedDateTime> expiryDate, CriteriaBuilder builder, Path<Boolean> active) {
    if (isDisplayed) {
      return prepareDisplayed(startDate, expiryDate, builder, active);
    } else {
      return prepareNotDisplayed(startDate, expiryDate, builder, active);
    }
  }

  private Predicate prepareDisplayed(Path<ZonedDateTime> startDate, Path<ZonedDateTime> expiryDate,
      CriteriaBuilder builder, Path<Boolean> active) {
    Predicate datesPredicate;
    if (expiryDate == null && startDate == null) {
      return builder.isTrue(active);
    } else if (expiryDate != null && startDate == null) {
      datesPredicate = builder.greaterThanOrEqualTo(expiryDate, NOW);
    } else if (expiryDate == null) {
      datesPredicate = builder.lessThanOrEqualTo(startDate, NOW);
    } else {
      datesPredicate = builder.and(builder.greaterThanOrEqualTo(expiryDate, NOW),
          builder.lessThanOrEqualTo(startDate, NOW));
    }
    return builder.and(datesPredicate, builder.isTrue(active));
  }

  private Predicate prepareNotDisplayed(Path<ZonedDateTime> startDate,
      Path<ZonedDateTime> expiryDate, CriteriaBuilder builder, Path<Boolean> active) {
    Predicate datesPredicate;
    if (expiryDate == null && startDate == null) {
      return builder.isFalse(active);
    } else if (expiryDate != null && startDate == null) {
      datesPredicate = builder.lessThan(expiryDate, NOW);
    } else if (expiryDate == null) {
      datesPredicate = builder.greaterThan(startDate, NOW);
    } else {
      datesPredicate = builder.or(builder.lessThan(expiryDate, NOW),
          builder.greaterThan(startDate, NOW));
    }
    return builder.or(datesPredicate, builder.isFalse(active));
  }

}
