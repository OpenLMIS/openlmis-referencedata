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

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.repository.custom.FacilityRepositoryCustom;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class FacilityRepositoryImpl implements FacilityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;


  /**
   * This method is supposed to retrieve all Facilities with facilityCode similar to
   * code parameter or facilityName similar to name parameter.
   * Method is ignoring case for facility code and name.
   * To find all wanted Facilities we use criteria query and like operator.
   *
   * @param code Part of wanted code.
   * @param name Part of wanted name.
   * @return List of Facilities with wanted code or name.
   */
  public List<Facility> findFacilitiesByCodeOrName(String code, String name) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Facility> query = builder.createQuery(Facility.class);
    Root<Facility> root = query.from(Facility.class);
    Predicate predicate = builder.disjunction();

    if (code != null) {
      predicate = builder.or(predicate,
          builder.like(builder.upper(root.get("code")), "%" + code.toUpperCase() + "%"));
    }

    if (name != null) {
      predicate = builder.or(predicate,
          builder.like(builder.upper(root.get("name")), "%" + name.toUpperCase() + "%"));
    }

    query.where(predicate);
    return entityManager.createQuery(query).getResultList();
  }

}
