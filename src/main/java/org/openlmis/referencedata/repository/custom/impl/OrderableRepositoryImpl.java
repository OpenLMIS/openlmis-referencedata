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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.SQLQuery;
import org.hibernate.type.LongType;
import org.hibernate.type.PostgresUUIDType;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.repository.custom.OrderableRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class OrderableRepositoryImpl implements OrderableRepositoryCustom {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(OrderableRepositoryImpl.class);

  private static final String FROM_ORDERABLES_TABLE = " FROM referencedata.orderables AS o";

  private static final String NATIVE_COUNT_ORDERABLES = "SELECT COUNT(*)" + FROM_ORDERABLES_TABLE;

  private static final String NATIVE_SELECT_ORDERABLES_IDENTITIES = "SELECT DISTINCT"
      + "   o.id AS id,"
      + "   o.versionId AS versionId"
      + FROM_ORDERABLES_TABLE;

  private static final String NATIVE_PROGRAM_ORDERABLE_INNER_JOIN =
      " INNER JOIN referencedata.program_orderables AS po"
          + "  ON o.id = po.orderableId AND o.versionId = po.orderableVersionId";

  private static final String NATIVE_PROGRAM_INNER_JOIN =
      " INNER JOIN referencedata.programs AS p"
          + "  ON p.id = po.programId";

  private static final String NATIVE_LATEST_ORDERABLE_INNER_JOIN =
      " INNER JOIN (SELECT id, MAX(versionId) AS versionId"
          + "   FROM referencedata.orderables GROUP BY id) AS latest"
          + "   ON o.id = latest.id AND o.versionId = latest.versionId";

  private static final String NATIVE_SELECT_ORDERABLES_BY_IDENTITES = "SELECT o.*"
      + FROM_ORDERABLES_TABLE;

  private static final String NATIVE_PAGEABLE = " LIMIT :limit OFFSET :offset";

  private static final String NATIVE_PRODUCT_CODE = "LOWER(o.code) LIKE :orderableCode";
  private static final String NATIVE_PRODUCT_NAME = "LOWER(o.fullProductName) LIKE :orderableName";
  private static final String NATIVE_PROGRAM_CODE = "LOWER(p.code) LIKE :programCode";
  private static final String NATIVE_IDENTITY = "(o.id = '%s' AND o.versionId = %d)";

  private static final String WHERE = " WHERE ";
  private static final String OR = " OR ";
  private static final String AND = " AND ";

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
    Query countNativeQuery = prepareNativeQuery(searchParams, true, pageable);
    int total = ((Number) countNativeQuery.getSingleResult()).intValue();

    if (total <= 0) {
      profiler.stop().log();
      return Pagination.getPage(Collections.emptyList(), pageable);
    }

    profiler.start("GET_VERSION_IDENTITY");
    Query nativeQuery = prepareNativeQuery(searchParams, false, pageable);
    Set<Pair<UUID, Long>> identities = executeNativeQuery(nativeQuery);

    profiler.start("RETRIEVE_ORDERABLES");
    List<Orderable> orderables = retrieveOrderables(identities);

    profiler.stop().log();
    return Pagination.getPage(orderables, pageable, total);
  }

  private Query prepareNativeQuery(SearchParams searchParams, boolean count, Pageable pageable) {
    String startNativeQuery = count ? NATIVE_COUNT_ORDERABLES : NATIVE_SELECT_ORDERABLES_IDENTITIES;
    StringBuilder builder = new StringBuilder(startNativeQuery);
    Map<String, Object> params = Maps.newHashMap();

    if (null != searchParams.getProgramCode()) {
      builder
          .append(NATIVE_PROGRAM_ORDERABLE_INNER_JOIN)
          .append(NATIVE_PROGRAM_INNER_JOIN)
          .append(AND)
          .append(NATIVE_PROGRAM_CODE);
      params.put("programCode", searchParams.getProgramCode().toLowerCase());
    }

    List<String> where = Lists.newArrayList();

    if (isEmpty(searchParams.getIdentityPairs())) {
      builder.append(NATIVE_LATEST_ORDERABLE_INNER_JOIN);
    } else {
      where.add(searchParams
          .getIdentityPairs()
          .stream()
          .map(pair -> String.format(NATIVE_IDENTITY, pair.getLeft(), pair.getRight()))
          .collect(Collectors.joining(OR)));
    }

    if (isNotBlank(searchParams.getCode())) {
      where.add(NATIVE_PRODUCT_CODE);
      params.put("orderableCode", "%" + searchParams.getCode().toLowerCase() + "%");
    }

    if (isNotBlank(searchParams.getName())) {
      where.add(NATIVE_PRODUCT_NAME);
      params.put("orderableName", "%" + searchParams.getName().toLowerCase() + "%");
    }

    if (!where.isEmpty()) {
      builder
          .append(WHERE)
          .append(String.join(AND, where));
    }

    if (!count) {
      setPagination(builder, params, pageable);
    }

    Query nativeQuery = entityManager.createNativeQuery(builder.toString());
    params.forEach(nativeQuery::setParameter);

    if (!count) {
      SQLQuery sqlQuery = nativeQuery.unwrap(SQLQuery.class);
      sqlQuery.addScalar("id", PostgresUUIDType.INSTANCE);
      sqlQuery.addScalar("versionId", LongType.INSTANCE);
    }

    return nativeQuery;
  }

  private void setPagination(StringBuilder builder, Map<String, Object> params, Pageable pageable) {
    Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);
    Integer limit = maxAndFirst.getLeft();
    Integer offset = maxAndFirst.getRight();

    if (limit > 0) {
      builder.append(NATIVE_PAGEABLE);
      params.put("limit", limit);
      params.put("offset", offset);
    }
  }

  private Set<Pair<UUID, Long>> executeNativeQuery(Query nativeQuery) {
    // appropriate configuration has been set in the native query
    @SuppressWarnings("unchecked")
    List<Object[]> identities = nativeQuery.getResultList();

    return identities
        .stream()
        .map(identity -> ImmutablePair.of((UUID) identity[0], (Long) identity[1]))
        .collect(Collectors.toSet());
  }

  // appropriate class has been passed in the EntityManager.createNativeQuery method
  @SuppressWarnings("unchecked")
  private List<Orderable> retrieveOrderables(Collection<Pair<UUID, Long>> identities) {
    String hql = NATIVE_SELECT_ORDERABLES_BY_IDENTITES + WHERE + identities
        .stream()
        .map(pair -> String.format(NATIVE_IDENTITY, pair.getLeft(), pair.getRight()))
        .collect(Collectors.joining(OR));

    return entityManager
        .createNativeQuery(hql, Orderable.class)
        .getResultList();
  }
}
