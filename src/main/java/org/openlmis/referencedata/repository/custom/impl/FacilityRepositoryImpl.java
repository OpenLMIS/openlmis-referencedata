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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.repository.custom.FacilityRepositoryCustom;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

public class FacilityRepositoryImpl implements FacilityRepositoryCustom {

  private static final String SELECT = "SELECT f"
      + " FROM Facility AS f"
      + " INNER JOIN FETCH f.geographicZone AS g"
      + " INNER JOIN FETCH f.type AS t"
      + " LEFT OUTER JOIN FETCH f.operator AS o"
      + " LEFT OUTER JOIN FETCH f.supportedPrograms AS sp";

  private static final String WHERE = "WHERE";
  private static final String OR = " OR ";

  private static final String WITH_CODE = "upper(f.code) LIKE :code";
  private static final String WITH_NAME = "upper(f.name) LIKE :name";
  private static final String WITH_ZONE = "g.id IN (:zones)";
  private static final String WITH_TYPE = "t.code = :typeCode";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all facilities with matched parameters.
   * Method is ignoring case for facility code and name.
   *
   * @param code Part of wanted code.
   * @param name Part of wanted name.
   * @param zones Geographic zones.
   * @param facilityTypeCode Wanted facility type.
   *
   * @return List of Facilities matching the parameters.
   */
  public List<Facility> search(String code, String name, List<GeographicZone> zones,
                               String facilityTypeCode) {
    List<String> hql = Lists.newArrayList(SELECT);
    List<String> where = Lists.newArrayList();
    Map<String, Object> params = Maps.newHashMap();


    if (null != code) {
      where.add(WITH_CODE);
      params.put("code", "%" + code.toUpperCase() + "%");
    }

    if (null != name) {
      where.add(WITH_NAME);
      params.put("name", "%" + name.toUpperCase() + "%");
    }

    if (null != facilityTypeCode) {
      where.add(WITH_TYPE);
      params.put("typeCode", facilityTypeCode);
    }

    if (isNotEmpty(zones)) {
      where.add(WITH_ZONE);
      params.put("zones", zones.stream().map(BaseEntity::getId).collect(Collectors.toSet()));
    }

    if (!where.isEmpty()) {
      hql.add(WHERE);
      hql.add(Joiner.on(OR).join(where));
    }

    String query = Joiner.on(' ').join(hql);

    TypedQuery<Facility> typed = entityManager.createQuery(query, Facility.class);
    params.forEach(typed::setParameter);

    return typed.getResultList();
  }
}
