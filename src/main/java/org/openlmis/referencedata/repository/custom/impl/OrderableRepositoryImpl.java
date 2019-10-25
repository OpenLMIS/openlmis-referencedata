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
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.commons.collections4.ListUtils;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.VersionIdentity;
import org.openlmis.referencedata.repository.custom.OrderableRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class OrderableRepositoryImpl implements OrderableRepositoryCustom {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(OrderableRepositoryImpl.class);

  private static final String FROM_ORDERABLES_TABLE = " FROM Orderable AS o";

  private static final String HQL_COUNT = "SELECT DISTINCT COUNT(*)"
      + FROM_ORDERABLES_TABLE;

  private static final String HQL_SELECT = "SELECT DISTINCT"
      + "   o.identity.id AS id,"
      + "   o.identity.versionNumber AS versionNumber,"
      + "   o.fullProductName AS fullProductName"
      + FROM_ORDERABLES_TABLE;

  private static final String PROGRAM_ORDERABLE_JOIN =
      " INNER JOIN o.programOrderables AS po";

  private static final String PROGRAM_JOIN =
      " INNER JOIN po.program AS p";

  private static final String LATEST_ORDERABLE_INNER_JOIN =
      " (o.identity.id, o.identity.versionNumber) IN ("
          + " SELECT identity.id, MAX(identity.versionNumber)"
          + " FROM Orderable GROUP BY identity.id)";

  private static final String GROUP_BY_ID_VERSION_NUMBER_AND_FULL_PRODUCT_NAME =
      " GROUP BY o.id, o.identity.versionNumber, o.fullProductName";

  private static final String WITH_PRODUCT_CODE = "LOWER(o.productCode.code) LIKE :orderableCode";
  private static final String WITH_PRODUCT_NAME = "LOWER(o.fullProductName) LIKE :orderableName";
  private static final String WITH_PROGRAM_CODE = "LOWER(p.code) LIKE :programCode";
  private static final String WITH_IDENTITY = "(o.identity.id = '%s'"
      + " AND o.identity.versionNumber = %d)";

  private static final String WHERE = " WHERE ";
  private static final String OR = " OR ";
  private static final String AND = " AND ";
  private static final String ORDER_BY = " ORDER BY ";
  private static final String ASC_SORT = " o.fullProductName ASC ";
  private static final String IDENTITY = "identity";

  // HQL queries are running into issues with bigger number of identities at once
  private static final Integer MAX_IDENTITIES_SIZE = 3000;

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all orderables with matched parameters. Method is ignoring
   * case for orderable code and name. To find all wanted orderables by code and name we use
   * criteria query and like operator.
   *
   * @return List of orderables matching the parameters.
   */
  @Override
  public Page<Orderable> search(SearchParams searchParams, Pageable pageable) {
    Profiler profiler = new Profiler("ORDERABLE_REPOSITORY_SEARCH_BY_PARAMS");
    profiler.setLogger(XLOGGER);

    profiler.start("CALCULATE_FULL_LIST_SIZE");
    Map<String, Object> params = Maps.newHashMap();
    Query countQuery = entityManager.createQuery(prepareNativeQuery(
        searchParams, HQL_COUNT, params), Long.class);
    params.forEach(countQuery::setParameter);
    Long total = (Long) countQuery.getSingleResult();

    if (total < 1) {
      profiler.stop().log();
      return Pagination.getPage(Collections.emptyList(), pageable, 0);
    }

    profiler.start("GET_VERSION_IDENTITY");
    params = Maps.newHashMap();
    String hqlWithSort = Joiner.on(' ').join(Lists.newArrayList(
        prepareNativeQuery(searchParams,
            HQL_SELECT, params),
        GROUP_BY_ID_VERSION_NUMBER_AND_FULL_PRODUCT_NAME,
        ORDER_BY, PageableUtil.getOrderPredicate(pageable, "o.", ASC_SORT)));

    Query searchQuery = entityManager.createQuery(hqlWithSort);
    params.forEach(searchQuery::setParameter);
    List<Object[]> identityList =  searchQuery
        .setMaxResults(pageable.getPageSize())
        .setFirstResult(pageable.getOffset())
        .getResultList();

    List<VersionIdentity> identities = executeNativeQuery(identityList);

    profiler.start("RETRIEVE_ORDERABLES");
    List<Orderable> orderables = new ArrayList<>();
    for (List<VersionIdentity> partition : ListUtils.partition(identities, MAX_IDENTITIES_SIZE)) {
      orderables.addAll(retrieveOrderables(partition, false));
    }

    profiler.stop().log();
    return Pagination.getPage(orderables, pageable, total);
  }

  /**
   * This method is supposed to get the latest last update date from the retrieved orderables
   * based on params passed to the request.
   *
   * @return ZonedDateTime of the latest last updated orderable.
   */
  @Override
  public List<Orderable> findOrderablesWithLatestModifiedDate(SearchParams searchParams,
      Pageable pageable) {
    Profiler profiler = new Profiler("ORDERABLE_REPOSITORY_SEARCH_LATEST_DATE_BY_PARAMS");
    profiler.setLogger(XLOGGER);

    profiler.start("CALCULATE_FULL_LIST_SIZE");
    Map<String, Object> params = Maps.newHashMap();
    Query countQuery = entityManager.createQuery(prepareNativeQuery(
        searchParams, HQL_COUNT + PROGRAM_ORDERABLE_JOIN + PROGRAM_JOIN, params), Long.class);
    params.forEach(countQuery::setParameter);
    Long total = (Long) countQuery.getSingleResult();

    if (total <= 0) {
      profiler.stop().log();
      return Collections.emptyList();
    }

    profiler.start("GET_VERSION_IDENTITY");
    params = Maps.newHashMap();
    String hqlWithSort = Joiner.on(' ').join(Lists.newArrayList(
        prepareNativeQuery(searchParams,
            HQL_SELECT + PROGRAM_ORDERABLE_JOIN + PROGRAM_JOIN, params),
        GROUP_BY_ID_VERSION_NUMBER_AND_FULL_PRODUCT_NAME,
        ORDER_BY, PageableUtil.getOrderPredicate(pageable, "o.", ASC_SORT)));

    Query searchQuery = entityManager.createQuery(hqlWithSort);
    params.forEach(searchQuery::setParameter);
    List<Object[]> identityList =  searchQuery
        .setMaxResults(pageable.getPageSize())
        .setFirstResult(pageable.getOffset())
        .getResultList();

    List<VersionIdentity> identities = executeNativeQuery(identityList);

    profiler.start("GET_ORDERABLE_WITH_LATEST_LAST_UPDATE_DATE_FROM_ORDERABLES");
    List<Orderable> orderables = new ArrayList<>();
    for (List<VersionIdentity> partition : ListUtils.partition(identities, MAX_IDENTITIES_SIZE)) {
      orderables.addAll(retrieveOrderables(partition, true));
    }

    profiler.stop().log();
    return orderables;
  }

  private String prepareNativeQuery(SearchParams searchParams, String baseSql,
      Map<String, Object> params) {

    List<String> sql = Lists.newArrayList(baseSql);
    List<String> where = Lists.newArrayList();

    if (null != searchParams) {
      if (null != searchParams.getProgramCode()) {
        sql.add(PROGRAM_ORDERABLE_JOIN + PROGRAM_JOIN);
        where.add(WITH_PROGRAM_CODE);
        params.put("programCode", Code.code(searchParams.getProgramCode().toLowerCase()));
      }

      if (isEmpty(searchParams.getIdentityPairs())) {
        where.add(LATEST_ORDERABLE_INNER_JOIN);
      } else {
        where.add(searchParams
            .getIdentityPairs()
            .stream()
            .map(pair -> String.format(WITH_IDENTITY, pair.getLeft(), pair.getRight()))
            .collect(Collectors.joining(OR)));
      }

      if (isNotBlank(searchParams.getCode())) {
        where.add(WITH_PRODUCT_CODE);
        params.put("orderableCode", "%" + searchParams.getCode().toLowerCase() + "%");
      }

      if (isNotBlank(searchParams.getName())) {
        where.add(WITH_PRODUCT_NAME);
        params.put("orderableName", "%" + searchParams.getName().toLowerCase() + "%");
      }
    } else {
      where.add(LATEST_ORDERABLE_INNER_JOIN);
    }

    if (!where.isEmpty()) {
      sql.add(WHERE);
      sql.add(String.join(AND, where));
    }

    return Joiner.on(' ').join(sql);
  }

  private List<VersionIdentity> executeNativeQuery(List<Object[]> identities) {

    return identities
        .stream()
        .map(identity -> new VersionIdentity((UUID) identity[0], (Long) identity[1]))
        .collect(Collectors.toList());
  }

  // appropriate class has been passed in the EntityManager.createQuery method
  @SuppressWarnings("unchecked")
  private List<Orderable> retrieveOrderables(Collection<VersionIdentity> identities,
      Boolean date) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Orderable> criteriaQuery =
        criteriaBuilder.createQuery(Orderable.class);
    Root<Orderable> root = criteriaQuery.from(Orderable.class);
    criteriaQuery.select(root).distinct(true).where(root.get(IDENTITY).in(identities));

    if (date) {
      criteriaQuery.orderBy(criteriaBuilder.desc(root.get("lastUpdated")));
    }

    return entityManager
        .createQuery(criteriaQuery)
        .getResultList();
  }

}
