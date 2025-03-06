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

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.repository.custom.LotRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class LotRepositoryImpl implements LotRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  private static final String EXPIRATION_DATE_FIELD = "expirationDate";

  /**
   * This method is supposed to retrieve all lots with matched parameters. Method is ignoring case
   * for lot code. To find all wanted lots by code and expiration date we use criteria query and
   * like operator.
   *
   * @param searchParams search params, not null
   * @return List of Facilities matching the parameters.
   */
  @Override
  public Page<Lot> search(SearchParams searchParams, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<Lot> lotQuery = builder.createQuery(Lot.class);
    lotQuery = prepareQuery(lotQuery, searchParams, false);

    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    countQuery = prepareQuery(countQuery, searchParams, true);

    Long count = entityManager.createQuery(countQuery).getSingleResult();

    Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);
    List<Lot> orderableList = entityManager.createQuery(lotQuery)
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();
    return Pagination.getPage(orderableList, pageable, count);
  }

  private <T> CriteriaQuery<T> prepareQuery(
      CriteriaQuery<T> query, SearchParams searchParams, boolean count) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    Root<Lot> root = query.from(Lot.class);

    if (count) {
      CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) query;
      query = (CriteriaQuery<T>) countQuery.select(builder.count(root));
    }

    Predicate predicate = builder.conjunction();

    if (isNotEmpty(searchParams.getTradeItems())) {
      predicate = builder.and(predicate, root.get("tradeItem").in(searchParams.getTradeItems()));
    }

    if (isNotEmpty(searchParams.getExactCodes())) {
      predicate =
          builder.and(predicate, root.get("lotCode").in(searchParams.getExactCodes()));
    } else if (searchParams.getCode() != null) {
      predicate = builder.and(predicate,
          builder.like(builder.upper(root.get("lotCode")),
              "%" + searchParams.getCode().toUpperCase() + "%"));
    }

    if (searchParams.getExpirationDate() != null) {
      predicate = builder.and(
              predicate,
              builder.equal(root.get(EXPIRATION_DATE_FIELD), searchParams.getExpirationDate())
      );
    }

    if (isNotEmpty(searchParams.getIds())) {
      predicate = builder.and(predicate, root.get("id").in(searchParams.getIds()));
    }

    if (searchParams.getExpirationDateFrom() != null) {
      predicate = builder.and(
              predicate,
              builder.greaterThanOrEqualTo(
                      root.get(EXPIRATION_DATE_FIELD), searchParams.getExpirationDateFrom()
              )
      );
    }

    if (searchParams.getExpirationDateTo() != null) {
      predicate = builder.and(
              predicate,
              builder.lessThanOrEqualTo(
                      root.get(EXPIRATION_DATE_FIELD), searchParams.getExpirationDateTo()
              )
      );
    }

    query.where(predicate);
    return query;
  }
}
