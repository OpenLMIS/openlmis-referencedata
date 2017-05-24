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

import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.repository.custom.OrderableRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class OrderableRepositoryImpl implements OrderableRepositoryCustom {

  private static final String CODE = "productCode";
  private static final String NAME = "fullProductName";
  private static final String PROGRAMS = "programOrderables";
  private static final String PROGRAM = "program";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all orderables with matched parameters.
   * Method is ignoring case for orderable code and name.
   * To find all wanted orderables by code and name we use criteria query and like operator.
   *
   * @param code Part of wanted code.
   * @param name Part of wanted name.
   * @param program Wanted program.
   * @return List of orderables matching the parameters.
   */
  public List<Orderable> search(String code, String name, Program program) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Orderable> query = builder.createQuery(Orderable.class);
    Root<Orderable> root = query.from(Orderable.class);
    Predicate predicate = builder.conjunction();

    if (code != null) {
      predicate = builder.and(predicate,
          builder.like(builder.upper(root.get(CODE).get("code")), "%" + code.toUpperCase() + "%"));
    }

    if (name != null) {
      predicate = builder.and(predicate,
          builder.like(builder.upper(root.get(NAME)), "%" + name.toUpperCase() + "%"));
    }

    if (program != null) {
      Join<Orderable, ProgramOrderable> orderablePrograms = root.join(PROGRAMS, JoinType.LEFT);
      predicate = builder.and(predicate, builder.equal(orderablePrograms.get(PROGRAM), program));
    }

    query.where(predicate);
    return entityManager.createQuery(query).getResultList();
  }
}
