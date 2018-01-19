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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.SQLQuery;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.PostgresUUIDType;
import org.hibernate.type.StringType;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.repository.custom.IdealStockAmountRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

  private static final String MINIMAL_SEARCH_SQL = "SELECT"
      + " id AS isa_id,"
      + " amount as isa_amount,"
      + " facilityid AS facility_id,"
      + " commoditytypeid AS commodity_id,"
      + " processingperiodid AS period_id"
      + " FROM referencedata.ideal_stock_amounts";

  private static final String COUNT_SEARCH_SQL = "SELECT"
      + " count(*) AS count"
      + " FROM referencedata.ideal_stock_amounts";

  private static final String WHERE = "WHERE";
  private static final String OR = " OR ";

  private static final String WITH_FACILITY_ID = "facilityid = :facilityId";
  private static final String WITH_COMMODITYTYPE_ID = "commoditytypeid = :commodityTypeId";
  private static final String WITH_PROCESSING_PERIOD_ID =
      "processingperiodid = :processingPeriodId";
  private static final int ISA_ID = 0;
  private static final int ISA_AMOUNT = 1;
  private static final int FACILITY_ID = 2;
  private static final int COMMODITY_ID = 3;
  private static final int PERIOD_ID = 4;

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

  /**
   * This method is supposed to retrieve all IdealStockAmounts that are present in given params.
   * List does not contain whole objects, just id f nested objects
   *
   * @return List of found Ideal Stock Amounts.
   */
  @Override
  public Page<IdealStockAmount> search(UUID facilityId, UUID commodityTypeId,
                                       UUID processingPeriodId, Pageable pageable) {
    Query query =
        createQuery(MINIMAL_SEARCH_SQL, facilityId, commodityTypeId, processingPeriodId);
    Query countQuery =
        createQuery(COUNT_SEARCH_SQL, facilityId, commodityTypeId, processingPeriodId);
    prepareMinimalQuery(query);
    prepareCountQuery(countQuery);

    // appropriate scalar is added to native query
    @SuppressWarnings("unchecked")
    List<Long> count = countQuery.getResultList();

    if (count.get(0) == 0) {
      return Pagination.getPage(Collections.emptyList(), pageable, 0);
    }

    Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);

    // hibernate returns a list of array of objects
    @SuppressWarnings("unchecked")
    List<Object[]> resultList = query
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();

    List<IdealStockAmount> result = resultList.stream()
        .map(this::toMinimalIsa)
        .collect(Collectors.toList());

    return Pagination.getPage(result, pageable, count.get(0));
  }

  private IdealStockAmount toIsa(Object[] values) {
    Facility facility = new Facility((String) values[5]);
    facility.setId((UUID) values[FACILITY_ID]);

    ProcessingSchedule schedule = new ProcessingSchedule();
    schedule.setCode((String) values[9]);

    ProcessingPeriod period = new ProcessingPeriod();
    period.setId((UUID) values[PERIOD_ID]);
    period.setName((String) values[8]);
    period.setProcessingSchedule(schedule);

    CommodityType commodityType = new CommodityType();
    commodityType.setId((UUID) values[COMMODITY_ID]);
    commodityType.setClassificationId((String) values[6]);
    commodityType.setClassificationSystem((String) values[7]);

    IdealStockAmount result = new IdealStockAmount(facility, commodityType,
        period, (Integer) values[ISA_AMOUNT]);
    result.setId((UUID) values[ISA_ID]);

    return result;
  }

  private IdealStockAmount toMinimalIsa(Object[] values) {
    Facility facility = new Facility((UUID) values[FACILITY_ID]);

    ProcessingPeriod period = new ProcessingPeriod();
    period.setId((UUID) values[PERIOD_ID]);

    CommodityType commodityType = new CommodityType();
    commodityType.setId((UUID) values[COMMODITY_ID]);

    IdealStockAmount result =
        new IdealStockAmount(facility, commodityType, period, (Integer) values[ISA_AMOUNT]);
    result.setId((UUID) values[ISA_ID]);

    return result;
  }

  private void prepareQuery(Query query) {
    SQLQuery sql = prepareMinimalQuery(query);
    sql.addScalar("facility_code", StringType.INSTANCE);
    sql.addScalar("classification_id", StringType.INSTANCE);
    sql.addScalar("classification_system", StringType.INSTANCE);
    sql.addScalar("period_name", StringType.INSTANCE);
    sql.addScalar("schedule_code", StringType.INSTANCE);
  }

  private SQLQuery prepareMinimalQuery(Query query) {
    SQLQuery sql = query.unwrap(SQLQuery.class);
    sql.addScalar("isa_id", PostgresUUIDType.INSTANCE);
    sql.addScalar("isa_amount", IntegerType.INSTANCE);
    sql.addScalar("facility_id", PostgresUUIDType.INSTANCE);
    sql.addScalar("commodity_id", PostgresUUIDType.INSTANCE);
    sql.addScalar("period_id", PostgresUUIDType.INSTANCE);
    return sql;
  }

  private void prepareCountQuery(Query query) {
    SQLQuery sql = query.unwrap(SQLQuery.class);
    sql.addScalar("count", LongType.INSTANCE);
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

  private Query createQuery(String searchSql, UUID facilityId,
                            UUID commodityTypeId, UUID processingPeriodId) {
    List<String> sql = Lists.newArrayList(searchSql);
    List<String> where = Lists.newArrayList();
    Map<String, Object> params = Maps.newHashMap();

    if (facilityId != null) {
      where.add(WITH_FACILITY_ID);
      params.put("facilityId", facilityId);
    }

    if (commodityTypeId != null) {
      where.add(WITH_COMMODITYTYPE_ID);
      params.put("commodityTypeId", commodityTypeId);
    }

    if (processingPeriodId != null) {
      where.add(WITH_PROCESSING_PERIOD_ID);
      params.put("processingPeriodId", processingPeriodId);
    }

    if (!where.isEmpty()) {
      sql.add(WHERE);
      sql.add(Joiner.on(OR).join(where));
    }

    String query = Joiner.on(' ').join(sql);

    Query nativeQuery = entityManager.createNativeQuery(query);
    params.forEach(nativeQuery::setParameter);
    return nativeQuery;
  }
}
