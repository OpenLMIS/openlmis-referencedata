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
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.repository.custom.SupplyLineRepositoryCustom;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class SupplyLineRepositoryImpl implements SupplyLineRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Method returns all Supply lines with matched parameters.
   *
   * @param program           program of searched Supply Lines.
   * @param supervisoryNode   supervisoryNode of searched Supply Lines.
   * @param supplyingFacility supplyingFacility of searched Supply Lines.
   * @return list of Supply Lines with matched parameters.
   */
  @Override
  public List<SupplyLine> searchSupplyLines(Program program, SupervisoryNode supervisoryNode,
                                            Facility supplyingFacility) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<SupplyLine> query = builder.createQuery(SupplyLine.class);
    Root<SupplyLine> root = query.from(SupplyLine.class);
    Predicate predicate = builder.conjunction();

    if (program != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("program"), program));
    }

    if (supervisoryNode != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("supervisoryNode"), supervisoryNode));
    }

    if (supplyingFacility != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("supplyingFacility"), supplyingFacility));
    }

    query.where(predicate);
    return entityManager.createQuery(query).getResultList();
  }


}
