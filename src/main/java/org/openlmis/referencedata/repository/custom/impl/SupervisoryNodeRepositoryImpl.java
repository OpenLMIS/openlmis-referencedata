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
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.repository.custom.SupervisoryNodeRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class SupervisoryNodeRepositoryImpl implements SupervisoryNodeRepositoryCustom {

  private static final String CODE = "code";
  private static final String NAME = "name";
  private static final String FACILITY = "facility";
  private static final String ZONE = "geographicZone";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all supervisory nodes with matched parameters.
   * Method is ignoring case and using like operator for code and name.
   *
   * @param code Part of wanted code.
   * @param name Part of wanted name.
   * @return List of Supervisory Nodes matching the parameters.
   */
  public List<SupervisoryNode> search(String code, String name, GeographicZone zone) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<SupervisoryNode> query = builder.createQuery(SupervisoryNode.class);
    Root<SupervisoryNode> root = query.from(SupervisoryNode.class);
    Predicate predicate = builder.conjunction();

    if (code != null) {
      predicate = builder.and(predicate,
          builder.like(builder.upper(root.get(CODE)), "%" + code.toUpperCase() + "%"));
    }

    if (name != null) {
      predicate = builder.and(predicate,
          builder.like(builder.upper(root.get(NAME)), "%" + name.toUpperCase() + "%"));
    }

    if (zone != null) {
      Join<SupervisoryNode, Facility> facilityJoin = root.join(FACILITY, JoinType.LEFT);
      predicate = builder.and(predicate, builder.equal(facilityJoin.get(ZONE), zone));
    }

    query.where(predicate);
    return entityManager.createQuery(query).getResultList();
  }
}
