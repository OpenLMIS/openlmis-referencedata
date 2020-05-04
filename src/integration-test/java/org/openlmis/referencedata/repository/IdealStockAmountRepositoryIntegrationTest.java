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

package org.openlmis.referencedata.repository;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.testbuilder.CommodityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.IdealStockAmountDataBuilder;
import org.openlmis.referencedata.testbuilder.ProcessingPeriodDataBuilder;
import org.openlmis.referencedata.testbuilder.ProcessingScheduleDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@SuppressWarnings("PMD.TooManyMethods")
public class IdealStockAmountRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<IdealStockAmount> {

  @Autowired
  private IdealStockAmountRepository isaRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private ProcessingPeriodRepository processingPeriodRepository;

  @Autowired
  private CommodityTypeRepository commodityTypeRepository;

  @Autowired
  private ProcessingScheduleRepository processingScheduleRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  private Facility facility;
  private ProcessingPeriod period;
  private ProcessingPeriod period2;
  private CommodityType commodityType;
  private UUID facilityId;
  private UUID commodityTypeId;
  private UUID processingPeriodId;

  IdealStockAmountRepository getRepository() {
    return this.isaRepository;
  }

  @Before
  public void setUp() {
    GeographicLevel geographicLevel = new GeographicLevelDataBuilder()
        .withLevelNumber(1)
        .buildAsNew();
    geographicLevelRepository.save(geographicLevel);

    GeographicZone geographicZone = new GeographicZoneDataBuilder()
        .withLevel(geographicLevel)
        .buildAsNew();
    geographicZoneRepository.save(geographicZone);

    FacilityType facilityType = new FacilityTypeDataBuilder().buildAsNew();
    facilityTypeRepository.save(facilityType);

    facility = new FacilityDataBuilder()
        .withType(facilityType)
        .withGeographicZone(geographicZone)
        .withoutOperator()
        .buildAsNew();
    facilityId = facilityRepository.save(facility).getId();

    commodityType = new CommodityTypeDataBuilder().buildAsNew();
    commodityTypeId = commodityTypeRepository.save(commodityType).getId();

    ProcessingSchedule schedule = new ProcessingScheduleDataBuilder().buildWithoutId();
    processingScheduleRepository.save(schedule);

    period = new ProcessingPeriodDataBuilder()
        .withStartDate(LocalDate.of(2017, 8, 25))
        .withEndDate(LocalDate.of(2017, 9, 25))
        .withSchedule(schedule)
        .buildAsNew();
    processingPeriodId = processingPeriodRepository.save(period).getId();

    period2 = new ProcessingPeriodDataBuilder()
        .withStartDate(LocalDate.of(2017, 9, 26))
        .withEndDate(LocalDate.of(2017, 10, 25))
        .withSchedule(schedule)
        .buildAsNew();
    processingPeriodRepository.save(period2);
  }

  IdealStockAmount generateInstance() {
    return new IdealStockAmountDataBuilder()
        .withFacility(facility)
        .withCommodityType(commodityType)
        .withProcessingPeriod(period)
        .buildAsNew();
  }

  @Test
  public void shouldGetIdsOfIdealStockAmounts() {
    IdealStockAmount isa = generateInstance();
    IdealStockAmount isa2 = generateInstance();
    isa2.setProcessingPeriod(period2);

    List<UUID> list = isaRepository.search(Lists.newArrayList(isa, isa2));
    assertThat(list, hasSize(0));

    isa = isaRepository.save(isa);
    isa2 = isaRepository.save(isa2);

    list = isaRepository.search(Lists.newArrayList(isa, isa2));

    assertEquals(2, list.size());
    assertTrue(list.contains(isa.getId()));
    assertTrue(list.contains(isa2.getId()));
  }

  @Test
  public void shouldGetPageOfIdealStockAmounts() {
    isaRepository.save(generateInstance());
    IdealStockAmount isa = isaRepository.save(generateInstance());

    Page<IdealStockAmount> page = isaRepository
        .search(facilityId, commodityTypeId, processingPeriodId, PageRequest.of(1, 1));

    checkPageProperties(page);
    checkIsaProperties(isa, page);
  }

  @Test
  public void shouldGetPageOfIdealStockAmountsWhenAnyParameterIsNull() {
    isaRepository.save(generateInstance());

    PageRequest pageable = PageRequest.of(0, 10);
    Page<IdealStockAmount> page = isaRepository
        .search(facilityId, commodityTypeId, null, pageable);
    assertEquals(1, page.getContent().size());
    page = isaRepository
        .search(facilityId, null, processingPeriodId, pageable);
    assertEquals(1, page.getContent().size());
    page = isaRepository
        .search(null, commodityTypeId, processingPeriodId, pageable);
    assertEquals(1, page.getContent().size());
  }

  @Test
  public void shouldGetEmptyPageOfIdealStockAmountsWhenAnyParameterIsWrong() {
    isaRepository.save(generateInstance());

    PageRequest pageable = PageRequest.of(0, 10);
    Page<IdealStockAmount> page = isaRepository
        .search(facilityId, commodityTypeId, UUID.randomUUID(), pageable);
    assertEquals(0, page.getContent().size());
    page = isaRepository
        .search(facilityId, UUID.randomUUID(), processingPeriodId, pageable);
    assertEquals(0, page.getContent().size());
    page = isaRepository
        .search(UUID.randomUUID(), commodityTypeId, processingPeriodId, pageable);
    assertEquals(0, page.getContent().size());
  }

  @Test
  public void shouldGetEmptyPageOfIdealStockAmountsWhenPageableIsNull() {
    Page<IdealStockAmount> page = isaRepository
        .search(null, null, null, PageRequest.of(0, 10));

    assertEquals(0, page.getContent().size());
  }

  @Test
  public void shouldGetAllIdealStockAmountsIfNoParamsProvided() {
    isaRepository.save(generateInstance());
    isaRepository.save(generateInstance());
    isaRepository.save(generateInstance());

    Page<IdealStockAmount> page = isaRepository
        .search(null, null, null, PageRequest.of(0, 10));

    assertEquals(3, page.getContent().size());
  }

  @Test
  public void shouldGetEmptyPageOfIdealStockAmountsIfMatchingIsaNotFound() {
    isaRepository.save(generateInstance());

    Page<IdealStockAmount> page = isaRepository
        .search(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), PageRequest.of(0, 10));

    assertEquals(0, page.getContent().size());
  }

  private void checkIsaProperties(IdealStockAmount isa, Page<IdealStockAmount> page) {
    IdealStockAmount idealStockAmount = page.getContent().get(0);
    assertEquals(isa.getId(), idealStockAmount.getId());
    assertEquals(Integer.valueOf(1000), idealStockAmount.getAmount());
    assertEquals(facilityId, idealStockAmount.getFacility().getId());
    assertEquals(commodityTypeId, idealStockAmount.getCommodityType().getId());
    assertEquals(processingPeriodId, idealStockAmount.getProcessingPeriod().getId());
  }

  private void checkPageProperties(Page<IdealStockAmount> page) {
    assertEquals(1, page.getContent().size());
    assertEquals(1, page.getSize());
    assertEquals(1, page.getNumber());
    assertEquals(1, page.getNumberOfElements());
    assertEquals(2, page.getTotalElements());
    assertEquals(2, page.getTotalPages());
  }
}
