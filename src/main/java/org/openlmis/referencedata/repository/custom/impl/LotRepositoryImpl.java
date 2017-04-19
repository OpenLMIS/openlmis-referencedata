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

import org.openlmis.referencedata.domain.Lot;
import org.openlmis.referencedata.domain.TradeItem;
import org.openlmis.referencedata.repository.custom.LotRepositoryCustom;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class LotRepositoryImpl implements LotRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all lots with matched parameters.
   * Method is ignoring case for lot code.
   * To find all wanted lots by code and expiration date we use criteria query and like operator.
   *
   * @param item TradeItem associated with Lot.
   * @param expirationDate date of lot expiration.
   * @param lotCode Part of wanted code.
   * @return List of Facilities matching the parameters.
   */
  public List<Lot> search(TradeItem item, LocalDate expirationDate, String lotCode) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Lot> query = builder.createQuery(Lot.class);
    Root<Lot> root = query.from(Lot.class);
    Predicate predicate = builder.disjunction();

    if (item != null) {
      predicate = builder.or(predicate, builder.equal(root.get("tradeItem"), item));
    }

    if (lotCode != null) {
      predicate = builder.or(predicate,
          builder.like(builder.upper(root.get("lotCode")), "%" + lotCode.toUpperCase() + "%"));
    }

    if (expirationDate != null) {
      predicate = builder.or(predicate, builder.equal(root.get("expirationDate"), expirationDate));
    }

    query.where(predicate);
    return entityManager.createQuery(query).getResultList();
  }
}
