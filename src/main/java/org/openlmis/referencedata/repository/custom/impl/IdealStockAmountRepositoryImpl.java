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

import static org.springframework.util.CollectionUtils.isEmpty;

import org.hibernate.SQLQuery;
import org.hibernate.type.IntegerType;
import org.hibernate.type.PostgresUUIDType;
import org.hibernate.type.StringType;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.repository.custom.IdealStockAmountRepositoryCustom;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class IdealStockAmountRepositoryImpl implements IdealStockAmountRepositoryCustom {

  private static final String SEARCH_SQL = "SELECT"
      + " isa.id AS isa_id, isa.amount as isa_amount,"
      + " f.id AS facility_id, f.code AS facility_code,"
      + " c.id AS commodity_id,  c.classificationid AS classification_id,"
      + " c.classificationsystem AS classification_system, p.id AS period_id,"
      + " p.name AS period_name, s.code AS schedule_code"
      + " FROM referencedata.ideal_stock_amounts isa"
      + " INNER JOIN referencedata.facilities f ON isa.facilityid = f.id"
      + " INNER JOIN referencedata.commodity_types c ON isa.commoditytypeid = c.id"
      + " INNER JOIN referencedata.processing_periods p ON isa.processingperiodid = p.id"
      + " INNER JOIN referencedata.processing_schedules s ON p.processingscheduleid = s.id";

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
  public List<IdealStockAmount> search(List<IdealStockAmount> idealStockAmounts) {
    Query query = createQuery(idealStockAmounts);
    prepareQuery(query);

    // hibernate always returns a list of array of objects
    @SuppressWarnings("unchecked")
    List<Object[]> list = Collections.checkedList(query.getResultList(), Object[].class);

    return list.stream().map(this::toIsa).collect(Collectors.toList());
  }

  private IdealStockAmount toIsa(Object[] values) {
    Facility facility = new Facility((String) values[3]);
    facility.setId((UUID) values[2]);

    ProcessingSchedule schedule = new ProcessingSchedule();
    schedule.setCode((String) values[9]);

    ProcessingPeriod period = new ProcessingPeriod();
    period.setId((UUID) values[7]);
    period.setName((String) values[8]);
    period.setProcessingSchedule(schedule);

    CommodityType commodityType = new CommodityType();
    commodityType.setId((UUID) values[4]);
    commodityType.setClassificationId((String) values[5]);
    commodityType.setClassificationSystem((String) values[6]);

    IdealStockAmount result = new IdealStockAmount(facility, commodityType,
        period, (Integer) values[1]);
    result.setId((UUID) values[0]);

    return result;
  }

  private void prepareQuery(Query query) {
    SQLQuery sql = query.unwrap(SQLQuery.class);
    sql.addScalar("isa_id", PostgresUUIDType.INSTANCE);
    sql.addScalar("isa_amount", IntegerType.INSTANCE);
    sql.addScalar("facility_id", PostgresUUIDType.INSTANCE);
    sql.addScalar("facility_code", StringType.INSTANCE);
    sql.addScalar("commodity_id", PostgresUUIDType.INSTANCE);
    sql.addScalar("classification_id", StringType.INSTANCE);
    sql.addScalar("classification_system", StringType.INSTANCE);
    sql.addScalar("period_id", PostgresUUIDType.INSTANCE);
    sql.addScalar("period_name", StringType.INSTANCE);
    sql.addScalar("schedule_code", StringType.INSTANCE);
  }

  private Query createQuery(List<IdealStockAmount> idealStockAmounts) {
    StringBuilder builder = new StringBuilder(SEARCH_SQL);

    if (!isEmpty(idealStockAmounts)) {
      builder.append(" WHERE");

      for (int i = 0, size = idealStockAmounts.size(); i < size; ++i) {
        IdealStockAmount isa = idealStockAmounts.get(i);
        builder
            .append(" (f.code = '")
            .append(isa.getFacility().getCode())
            .append("' AND c.classificationid = '")
            .append(isa.getCommodityType().getClassificationId())
            .append("' AND c.classificationsystem = '")
            .append(isa.getCommodityType().getClassificationSystem())
            .append("' AND p.name = '")
            .append(isa.getProcessingPeriod().getName())
            .append("' AND s.code = '")
            .append(isa.getProcessingPeriod().getProcessingSchedule().getCode())
            .append("')");

        if (i + 1 < size) {
          builder.append(" OR");
        }
      }
    }

    return entityManager.createNativeQuery(builder.toString());
  }
}
