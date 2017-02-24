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
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.repository.custom.FacilityRepositoryCustom;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class FacilityRepositoryImpl implements FacilityRepositoryCustom {

  private static final String GEOGRAPHIC_ZONE = "geographicZone";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all facilities with matched parameters.
   * Method is ignoring case for facility code and name.
   * To find all wanted Facilities by conde and name we use criteria query and like operator.
   *
   * @param code Part of wanted code.
   * @param name Part of wanted name.
   * @param zone Geographic zone of facility location.
   * @return List of Facilities matching the parameters.
   */
  public List<Facility> search(String code, String name, GeographicZone zone) {
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

    if (zone != null) {
      if (name == null && code == null) {
        predicate = builder.or(predicate, builder.equal(root.get(GEOGRAPHIC_ZONE), zone));
      } else {
        predicate = builder.and(predicate, builder.equal(root.get(GEOGRAPHIC_ZONE), zone));
      }
    }

    query.where(predicate);
    return entityManager.createQuery(query).getResultList();
  }

  /**
   * Retrieve all facilities within given geographic zones.
   *
   * @param zones Geographic zones to match facility location.
   * @return List of matched facilities.
   */
  public List<Facility> search(Collection<GeographicZone> zones) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Facility> query = builder.createQuery(Facility.class);
    Root<Facility> root = query.from(Facility.class);
    Predicate predicate = builder.disjunction();

    if (zones != null) {
      predicate = builder.or(predicate, root.get(GEOGRAPHIC_ZONE).in(zones));
    }

    query.where(predicate);
    return entityManager.createQuery(query).getResultList();
  }
}
