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

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.SQLQuery;
import org.hibernate.type.PostgresUUIDType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.repository.custom.FacilityRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class FacilityRepositoryImpl implements FacilityRepositoryCustom {

  private static final String NATIVE_SELECT_BY_PARAMS = "SELECT DISTINCT f.id AS ID"
      + " FROM referencedata.facilities AS f"
      + " INNER JOIN referencedata.geographic_zones AS g ON f.geographiczoneid = g.id"
      + " INNER JOIN referencedata.facility_types AS t ON f.typeid = t.id";

  private static final String HQL_SELECT_BY_IDS = "SELECT DISTINCT f"
      + " FROM Facility AS f"
      + " INNER JOIN FETCH f.geographicZone AS g"
      + " INNER JOIN FETCH f.type AS t"
      + " LEFT OUTER JOIN FETCH f.operator AS o"
      + " LEFT OUTER JOIN FETCH f.supportedPrograms AS sp"
      + " WHERE f.id in (:ids)";

  private static final String WHERE = "WHERE";
  private static final String OR = " OR ";
  private static final String AND = " AND ";
  private static final String DEFAULT_SORT = "f.name ASC";
  private static final String ASC = "ASC";
  private static final String DESC = "DESC";
  private static final String ORDER_BY = "ORDER BY";

  private static final String WITH_CODE = "UPPER(f.code) LIKE :code";
  private static final String WITH_NAME = "UPPER(f.name) LIKE :name";
  private static final String WITH_ZONE = "g.id IN (:zones)";
  private static final String WITH_TYPE = "t.code = :typeCode";
  private static final String WITH_EXTRA_DATA = "f.extradata @> (:extraData)\\:\\:jsonb";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all facilities with matched parameters.
   * Method is ignoring case for facility code and name.
   *
   * @param code              Part of wanted code.
   * @param name              Part of wanted name.
   * @param geographicZoneIds Geographic zone IDs.
   * @param facilityTypeCode  Wanted facility type.
   * @param extraData         extra data
   * @return List of Facilities matching the parameters.
   */
  public Page<Facility> search(String code, String name, Set<UUID> geographicZoneIds,
                               String facilityTypeCode, String extraData,
                               Boolean conjunction, Pageable pageable) {
    List<String> sql = Lists.newArrayList(NATIVE_SELECT_BY_PARAMS);
    List<String> where = Lists.newArrayList();
    Map<String, Object> params = Maps.newHashMap();


    if (isNotBlank(code)) {
      where.add(WITH_CODE);
      params.put("code", "%" + code.toUpperCase() + "%");
    }

    if (isNotBlank(name)) {
      where.add(WITH_NAME);
      params.put("name", "%" + name.toUpperCase() + "%");
    }

    if (isNotBlank(facilityTypeCode)) {
      where.add(WITH_TYPE);
      params.put("typeCode", facilityTypeCode);
    }

    if (isNotEmpty(geographicZoneIds)) {
      where.add(WITH_ZONE);
      params.put("zones", geographicZoneIds);
    }

    if (isNotBlank(extraData)) {
      where.add(WITH_EXTRA_DATA);
      params.put("extraData", extraData);
    }

    if (!where.isEmpty()) {
      sql.add(WHERE);
      sql.add(Joiner.on(conjunction ? AND : OR).join(where));
    }

    String query = Joiner.on(' ').join(sql);

    Query nativeQuery = entityManager.createNativeQuery(query);
    params.forEach(nativeQuery::setParameter);

    SQLQuery sqlQuery = nativeQuery.unwrap(SQLQuery.class);
    sqlQuery.addScalar("ID", PostgresUUIDType.INSTANCE);

    // appropriate scalar is added to native query
    @SuppressWarnings("unchecked")
    List<UUID> ids = nativeQuery.getResultList();

    Integer count = ids.size();

    if (isEmpty(ids)) {
      return Pagination.getPage(Collections.emptyList(), pageable, 0);
    }

    Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);

    String hqlWithSort = Joiner.on(' ').join(Lists.newArrayList(HQL_SELECT_BY_IDS, ORDER_BY,
            getOrderPredicate(pageable)));

    List<Facility> facilities =  entityManager
        .createQuery(hqlWithSort, Facility.class)
        .setParameter("ids", ids)
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();

    return Pagination.getPage(facilities, pageable, count);
  }

  private String getOrderPredicate(Pageable pageable) {
    if (pageable.getSort() != null) {
      List<String> orderPredicate = new ArrayList<String>();
      List<String> sql = new ArrayList<String>();
      Iterator<Sort.Order> iterator = pageable.getSort().iterator();
      Sort.Order order;
      Sort.Direction sortDirection = Sort.Direction.ASC;

      while (iterator.hasNext()) {
        order = iterator.next();
        orderPredicate.add("f.".concat(order.getProperty()));
        sortDirection = order.getDirection();
      }

      sql.add(Joiner.on(",").join(orderPredicate));
      sql.add(sortDirection.isAscending() ? ASC : DESC);

      return Joiner.on(' ').join(sql);
    }

    return DEFAULT_SORT;
  }
}
