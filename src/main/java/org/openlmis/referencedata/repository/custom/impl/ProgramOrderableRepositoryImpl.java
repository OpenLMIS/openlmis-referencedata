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

import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.repository.custom.ProgramOrderableRepositoryCustom;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class ProgramOrderableRepositoryImpl implements ProgramOrderableRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Finds ProgramOrderables matching all of provided parameters.
   * @param program program of searched ProgramOrderables.
   * @return list of all ProgramOrderables matching all of provided parameters.
   */
  public List<ProgramOrderable> searchProgramOrderables(Program program) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ProgramOrderable> query = builder.createQuery(ProgramOrderable.class);
    Root<ProgramOrderable> root = query.from(ProgramOrderable.class);
    Predicate predicate = builder.conjunction();

    if (program != null) {
      predicate = builder.and(
              predicate,
              builder.equal(
                      root.get("program"), program));
    }
    query.where(predicate);
    return entityManager.createQuery(query).getResultList();
  }
}
