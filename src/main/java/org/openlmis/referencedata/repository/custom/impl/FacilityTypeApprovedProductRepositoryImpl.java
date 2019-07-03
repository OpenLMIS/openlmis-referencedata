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
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.custom.FacilityTypeApprovedProductRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.FacilityMessageKeys;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class FacilityTypeApprovedProductRepositoryImpl
    implements FacilityTypeApprovedProductRepositoryCustom {

  private static final XLogger XLOGGER =
      XLoggerFactory.getXLogger(FacilityTypeApprovedProductRepositoryImpl.class);

  private static final String NATIVE_SELECT_FACILITY_TYPE_ID = "SELECT ft.id AS type_id"
      + " FROM referencedata.facility_types AS ft"
      + " INNER JOIN referencedata.facilities f ON f.typeId = ft.id AND f.id = '%s'";

  private static final String FROM_FTAP_TABLE =
      " FROM referencedata.facility_type_approved_products AS ftap";

  private static final String NATIVE_COUNT_FTAPS = "SELECT COUNT(*)" + FROM_FTAP_TABLE;

  private static final String NATIVE_SELECT_FTAP_IDENTITIES = "SELECT DISTINCT"
      + "   ftap.id AS id,"
      + "   ftap.versionId AS versionId"
      + FROM_FTAP_TABLE;
  private static final String NATIVE_PROGRAM_INNER_JOIN =
      " INNER JOIN referencedata.programs AS p ON p.id = ftap.programId";
  private static final String NATIVE_ORDERABLE_INNER_JOIN =
      " INNER JOIN (SELECT id, MAX(versionId) AS versionId"
          + "   FROM referencedata.orderables GROUP BY id) AS o"
          + "   ON o.id = ftap.orderableId";
  private static final String NATIVE_PROGRAM_ORDERABLE_INNER_JOIN =
      " INNER JOIN referencedata.program_orderables AS po"
          + " ON o.id = po.orderableId"
          + " AND o.versionId = po.orderableVersionId"
          + " AND p.id = po.programId"
          + " AND po.active IS TRUE";
  private static final String NATIVE_FACILITY_TYPE_INNER_JOIN =
      " INNER JOIN referencedata.facility_types AS ft ON ft.id = ftap.facilityTypeId";
  private static final String NATIVE_LATEST_FTAPS_INNER_JOIN =
      " INNER JOIN (SELECT id, MAX(versionId) AS versionId"
          + "   FROM referencedata.facility_type_approved_products GROUP BY id) AS latest"
          + "   ON ftap.id = latest.id AND ftap.versionId = latest.versionId";
  private static final String NATIVE_FTAP_ACTIVE_FLAG = " ftap.active = :active";

  private static final String NATIVE_PAGEABLE = " LIMIT :limit OFFSET :offset";

  private static final String NATIVE_SELECT_FTAPS_BY_IDENTITES = "SELECT ftap.*" + FROM_FTAP_TABLE;

  private static final String NATIVE_IDENTITY = "(ftap.id = '%s' AND ftap.versionId = %d)";
  private static final String WHERE = " WHERE ";
  private static final String OR = " OR ";
  private static final String AND = " AND ";

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Page<FacilityTypeApprovedProduct> searchProducts(UUID facilityId, UUID programId,
      Boolean fullSupply, List<UUID> orderableIds, Boolean active, Pageable pageable) {

    Profiler profiler = new Profiler("FTAP_REPOSITORY_SEARCH");
    profiler.setLogger(XLOGGER);

    profiler.start("SEARCH_FACILITY_TYPE_ID");
    UUID facilityTypeId = getFacilityTypeId(facilityId, profiler);

    profiler.start("CALCULATE_FULL_LIST_SIZE");
    Query countNativeQuery = prepareNativeQuery(facilityTypeId, programId, fullSupply, orderableIds,
        active, true, pageable);

    int total = executeCountQuery(countNativeQuery);

    if (total <= 0) {
      profiler.stop().log();
      return Pagination.getPage(Collections.emptyList());
    }

    profiler.start("GET_VERSION_IDENTITY");
    Query nativeQuery = prepareNativeQuery(facilityTypeId, programId, fullSupply, orderableIds,
        active, false, pageable);
    Set<Pair<UUID, Long>> identities = executeNativeQuery(nativeQuery);

    profiler.start("RETRIEVE_FTAPS");
    List<FacilityTypeApprovedProduct> ftaps = retrieveFtaps(identities);

    profiler.stop().log();
    return Pagination.getPage(ftaps, pageable, total);
  }

  @Override
  public Page<FacilityTypeApprovedProduct> searchProducts(SearchParams searchParams,
      Pageable pageable) {
    Profiler profiler = new Profiler("FTAP_REPOSITORY_SEARCH_BY_PARAMS");
    profiler.setLogger(XLOGGER);

    profiler.start("CALCULATE_FULL_LIST_SIZE");
    Query countNativeQuery = prepareNativeQuery(searchParams, true, pageable);
    int total = executeCountQuery(countNativeQuery);

    if (total <= 0) {
      profiler.stop().log();
      return Pagination.getPage(Collections.emptyList());
    }

    profiler.start("GET_VERSION_IDENTITY");
    Query nativeQuery = prepareNativeQuery(searchParams, false, pageable);
    Set<Pair<UUID, Long>> identities = executeNativeQuery(nativeQuery);

    profiler.start("RETRIEVE_FTAPS");
    List<FacilityTypeApprovedProduct> ftaps = retrieveFtaps(identities);

    profiler.stop().log();
    return Pagination.getPage(ftaps, pageable, total);
  }

  private UUID getFacilityTypeId(UUID facilityId, Profiler profiler) {
    String queryString = String.format(NATIVE_SELECT_FACILITY_TYPE_ID, facilityId);
    Query query = entityManager.createNativeQuery(queryString);

    SQLQuery sql = query.unwrap(SQLQuery.class);
    sql.addScalar("type_id", PostgresUUIDType.INSTANCE);

    try {
      return (UUID) query.getSingleResult();
    } catch (Exception ex) {
      profiler.stop().log();
      throw new ValidationMessageException(ex, FacilityMessageKeys.ERROR_NOT_FOUND);
    }
  }

  private Query prepareNativeQuery(UUID facilityTypeId, UUID programId, Boolean fullSupply,
      List<UUID> orderableIds, Boolean active, boolean count, Pageable pageable) {
    String startNativeQuery = count ? NATIVE_COUNT_FTAPS : NATIVE_SELECT_FTAP_IDENTITIES;
    StringBuilder builder = new StringBuilder(startNativeQuery);
    Map<String, Object> params = Maps.newHashMap();

    builder.append(NATIVE_PROGRAM_INNER_JOIN);
    if (null != programId) {
      builder.append(" AND p.id = :programId");
      params.put("programId", programId);
    }

    builder.append(NATIVE_ORDERABLE_INNER_JOIN);
    if (!isEmpty(orderableIds)) {
      builder.append(" AND o.id in (:orderableIds)");
      params.put("orderableIds", orderableIds);
    }

    builder.append(NATIVE_PROGRAM_ORDERABLE_INNER_JOIN);
    if (null != fullSupply) {
      builder.append(" AND po.fullSupply = :fullSupply");
      params.put("fullSupply", fullSupply);
    }

    builder.append(NATIVE_FACILITY_TYPE_INNER_JOIN);
    if (null != facilityTypeId) {
      builder.append(" AND ft.id = :facilityTypeId");
      params.put("facilityTypeId", facilityTypeId);
    }

    builder
        .append(NATIVE_LATEST_FTAPS_INNER_JOIN)
        .append(WHERE)
        .append(NATIVE_FTAP_ACTIVE_FLAG);
    params.put("active", null == active || active);

    if (!count) {
      setPagination(builder, params, pageable);
    }

    return createNativeQuery(builder, params, count);
  }

  private Query prepareNativeQuery(SearchParams searchParams, boolean count, Pageable pageable) {
    String startNativeQuery = count ? NATIVE_COUNT_FTAPS : NATIVE_SELECT_FTAP_IDENTITIES;
    StringBuilder builder = new StringBuilder(startNativeQuery);
    Map<String, Object> params = Maps.newHashMap();

    builder.append(NATIVE_PROGRAM_INNER_JOIN);
    if (isNotBlank(searchParams.getProgramCode())) {
      builder.append(" AND p.code = :programCode");
      params.put("programCode", searchParams.getProgramCode());
    }

    builder
        .append(NATIVE_ORDERABLE_INNER_JOIN)
        .append(NATIVE_PROGRAM_ORDERABLE_INNER_JOIN)
        .append(NATIVE_FACILITY_TYPE_INNER_JOIN);

    if (!isEmpty(searchParams.getFacilityTypeCodes())) {
      builder.append(" AND ft.code in (:facilityTypeCodes)");
      params.put("facilityTypeCodes", searchParams.getFacilityTypeCodes());
    }

    List<String> where = Lists.newArrayList();

    if (isEmpty(searchParams.getIdentityPairs())) {
      builder.append(NATIVE_LATEST_FTAPS_INNER_JOIN);
    } else {
      where.add(searchParams
          .getIdentityPairs()
          .stream()
          .map(pair -> String.format(NATIVE_IDENTITY, pair.getLeft(), pair.getRight()))
          .collect(Collectors.joining(OR)));
    }

    if (null != searchParams.getActive()) {
      where.add(NATIVE_FTAP_ACTIVE_FLAG);
      params.put("active", searchParams.getActive());
    }

    if (!isEmpty(where)) {
      builder
          .append(WHERE)
          .append(String.join(AND, where));
    }

    if (!count) {
      setPagination(builder, params, pageable);
    }

    return createNativeQuery(builder, params, count);
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

  private Query createNativeQuery(StringBuilder builder, Map<String, Object> params,
      boolean count) {
    Query nativeQuery = entityManager.createNativeQuery(builder.toString());
    params.forEach(nativeQuery::setParameter);

    if (!count) {
      SQLQuery sqlQuery = nativeQuery.unwrap(SQLQuery.class);
      sqlQuery.addScalar("id", PostgresUUIDType.INSTANCE);
      sqlQuery.addScalar("versionId", LongType.INSTANCE);
    }

    return nativeQuery;
  }

  private int executeCountQuery(Query nativeQuery) {
    return ((Number) nativeQuery.getSingleResult()).intValue();
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
  private List<FacilityTypeApprovedProduct> retrieveFtaps(Collection<Pair<UUID, Long>> identities) {
    String hql = NATIVE_SELECT_FTAPS_BY_IDENTITES + WHERE + identities
        .stream()
        .map(pair -> String.format(NATIVE_IDENTITY, pair.getLeft(), pair.getRight()))
        .collect(Collectors.joining(OR));

    return entityManager
        .createNativeQuery(hql, FacilityTypeApprovedProduct.class)
        .getResultList();
  }

}
