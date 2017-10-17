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

import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.repository.custom.IdealStockAmountRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IdealStockAmountRepositoryImpl implements IdealStockAmountRepositoryCustom {

  private static final String CODE = "code";
  private static final String FACILITY = "facility";
  private static final String PROCESSING_PERIOD = "processingPeriod";
  private static final String NAME = "name";
  private static final String PROCESSING_SCHEDULE = "processingSchedule";
  private static final String COMMODITY_TYPE = "commodityType";
  private static final String CLASSIFICATION_ID = "classificationId";
  private static final String CLASSIFICATION_SYSTEM = "classificationSystem";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all IdealStockAmounts that are present in given list.
   * List does not have to contain whole objects, just properties that will be used:
   * facility.code, processingPeriod.name, processingPeriod.processingSchedule.code,
   * commodityType.classificationId, commodityType.classificationSystem
   *
   * @param idealStockAmounts list of ideal stock amounts with required fields
   * @return List of found Ideal Stock Amounts.
   */
  public List<IdealStockAmount> search(Collection<IdealStockAmount> idealStockAmounts) {

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<IdealStockAmount> query = builder.createQuery(IdealStockAmount.class);
    Root<IdealStockAmount> root = query.from(IdealStockAmount.class);

    Join<IdealStockAmount, Facility> facilityJoin = root.join(FACILITY);
    Join<IdealStockAmount, ProcessingPeriod> periodJoin = root.join(PROCESSING_PERIOD);
    Join<IdealStockAmount, ProcessingSchedule> scheduleJoin = periodJoin.join(PROCESSING_SCHEDULE);
    Join<IdealStockAmount, CommodityType> commodityTypeJoin = root.join(COMMODITY_TYPE);

    List<Predicate> predicates = new ArrayList<>();
    for (IdealStockAmount isa : idealStockAmounts) {
      predicates.add(builder.and(builder.equal(facilityJoin.get(CODE), isa.getFacility().getCode()),
          builder.equal(periodJoin.get(NAME), isa.getProcessingPeriod().getName()),
          builder.equal(scheduleJoin.get(CODE),
              isa.getProcessingPeriod().getProcessingSchedule().getCode()),
          builder.equal(commodityTypeJoin.get(CLASSIFICATION_ID),
              isa.getCommodityType().getClassificationId()),
          builder.equal(commodityTypeJoin.get(CLASSIFICATION_SYSTEM),
              isa.getCommodityType().getClassificationSystem())));
    }

    query.where(builder.or(predicates.toArray(new Predicate[idealStockAmounts.size()])));
    return entityManager.createQuery(query).getResultList();
  }
}
