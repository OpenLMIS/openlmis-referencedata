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

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.SQLQuery;
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

  private static final String NATIVE_SELECT_FTAP_IDS = "SELECT DISTINCT ftap.id AS ID"
      + " FROM referencedata.facility_type_approved_products AS ftap";
  private static final String NATIVE_PROGRAM_INNER_JOIN =
      " INNER JOIN referencedata.programs AS p ON p.id = ftap.programId";
  private static final String NATIVE_ORDERABLE_INNER_JOIN =
      " INNER JOIN referencedata.orderables AS o"
          + " ON o.id = ftap.orderableId"
          + " AND o.versionId IN ("
          + "   SELECT MAX(versionId)"
          + "   FROM referencedata.orderables"
          + "   WHERE id = o.id)";
  private static final String NATIVE_PROGRAM_ORDERABLE_INNER_JOIN =
      " INNER JOIN referencedata.program_orderables AS po"
          + " ON o.id = po.orderableId"
          + " AND o.versionId = po.orderableVersionId"
          + " AND p.id = po.programId"
          + " AND po.active IS TRUE";
  private static final String NATIVE_FACILITY_TYPE_INNER_JOIN =
      " INNER JOIN referencedata.facility_types AS ft ON ft.id = ftap.facilityTypeId";
  private static final String HQL_SELECT_FTAP_BY_IDS = "SELECT ftap"
      + " FROM FacilityTypeApprovedProduct AS ftap"
      + " WHERE ftap.id IN (:ids)";
  private static final String NATIVE_WHERE_FTAP_ACTIVE_FLAG = " WHERE ftap.active = :active";

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Page<FacilityTypeApprovedProduct> searchProducts(UUID facilityId, UUID programId,
      Boolean fullSupply, List<UUID> orderableIds, Boolean active, Pageable pageable) {

    Profiler profiler = new Profiler("FTAP_REPOSITORY_SEARCH");
    profiler.setLogger(XLOGGER);

    profiler.start("SEARCH_FACILITY_TYPE_ID");
    UUID facilityTypeId = getFacilityTypeId(facilityId, profiler);

    Query nativeQuery = prepareQuery(facilityTypeId, programId, fullSupply, orderableIds,
        active, pageable);
    return executeQuery(nativeQuery, pageable);
  }

  @Override
  public Page<FacilityTypeApprovedProduct> searchProducts(List<String> facilityTypeCodes,
      String programCode, Boolean active, Pageable pageable) {
    Query nativeQuery = prepareQuery(facilityTypeCodes, programCode, active, pageable);
    return executeQuery(nativeQuery, pageable);

  }

  private Page<FacilityTypeApprovedProduct> executeQuery(Query query, Pageable pageable) {
    // appropriate scalar is added to native query
    @SuppressWarnings("unchecked")
    List<UUID> ids = query.getResultList();

    if (CollectionUtils.isEmpty(ids)) {
      return Pagination.getPage(Collections.emptyList(), pageable, 0);
    }

    Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);
    Integer limit = maxAndFirst.getLeft();
    Integer offset = maxAndFirst.getRight();

    List<FacilityTypeApprovedProduct> resultList = entityManager
        .createQuery(HQL_SELECT_FTAP_BY_IDS, FacilityTypeApprovedProduct.class)
        .setParameter("ids", ids)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();

    return Pagination.getPage(resultList, pageable, ids.size());
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

  private Query prepareQuery(UUID facilityTypeId, UUID programId, Boolean fullSupply,
      List<UUID> orderableIds, Boolean active, Pageable pageable) {
    StringBuilder builder = new StringBuilder(NATIVE_SELECT_FTAP_IDS);
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

    builder.append(NATIVE_WHERE_FTAP_ACTIVE_FLAG);
    params.put("active", null == active || active);

    return createQuery(builder, params);
  }

  private Query prepareQuery(List<String> facilityTypeCodes,
      String programCode, Boolean active, Pageable pageable) {
    StringBuilder builder = new StringBuilder(NATIVE_SELECT_FTAP_IDS);
    Map<String, Object> params = Maps.newHashMap();

    builder.append(NATIVE_PROGRAM_INNER_JOIN);
    if (isNotBlank(programCode)) {
      builder.append(" AND p.code = :programCode");
      params.put("programCode", programCode);
    }

    builder
        .append(NATIVE_ORDERABLE_INNER_JOIN)
        .append(NATIVE_PROGRAM_ORDERABLE_INNER_JOIN)
        .append(NATIVE_FACILITY_TYPE_INNER_JOIN);

    if (!isEmpty(facilityTypeCodes)) {
      builder.append(" AND ft.code in (:facilityTypeCodes)");
      params.put("facilityTypeCodes", facilityTypeCodes);
    }

    builder.append(NATIVE_WHERE_FTAP_ACTIVE_FLAG);
    params.put("active", null == active || active);

    return createQuery(builder, params);
  }

  private Query createQuery(StringBuilder builder, Map<String, Object> params) {

    Query nativeQuery = entityManager.createNativeQuery(builder.toString());
    params.forEach(nativeQuery::setParameter);

    SQLQuery sqlQuery = nativeQuery.unwrap(SQLQuery.class);
    sqlQuery.addScalar("ID", PostgresUUIDType.INSTANCE);

    return nativeQuery;
  }

}
