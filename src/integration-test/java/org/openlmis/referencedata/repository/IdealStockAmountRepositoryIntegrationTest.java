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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
  private ProcessingSchedule schedule;
  private CommodityType commodityType;
  private UUID facilityId;
  private UUID commodityTypeId;
  private UUID processingPeriodId;
  private Integer amount;

  IdealStockAmountRepository getRepository() {
    return this.isaRepository;
  }

  @Before
  public void setUp() {
    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode("geographic-level");
    geographicLevel.setLevelNumber(1);
    geographicLevelRepository.save(geographicLevel);

    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode("geographic-zone");
    geographicZone.setLevel(geographicLevel);
    geographicZoneRepository.save(geographicZone);

    FacilityType facilityType = new FacilityType();
    facilityType.setCode("facility-type");
    facilityTypeRepository.save(facilityType);

    facility = new Facility("facility-code");
    facility.setGeographicZone(geographicZone);
    facility.setActive(true);
    facility.setEnabled(true);
    facility.setType(facilityType);
    facility.setName("Facility");
    facility.setDescription("facility description");
    facilityId = facilityRepository.save(facility).getId();

    commodityType = new CommodityType("Name", "cSys", "cId", null, new ArrayList<>());
    commodityTypeId = commodityTypeRepository.save(commodityType).getId();

    schedule = new ProcessingSchedule();
    schedule.setCode(Code.code("schedule-code"));
    schedule.setDescription("desc");
    schedule.setId(UUID.randomUUID());
    schedule.setModifiedDate(ZonedDateTime.now());
    schedule.setName("schedule");
    processingScheduleRepository.save(schedule);

    period = new ProcessingPeriod();
    period.setProcessingSchedule(schedule);
    period.setName("period");
    period.setStartDate(LocalDate.of(2017, 8, 25));
    period.setEndDate(LocalDate.of(2017, 9, 25));
    processingPeriodId = processingPeriodRepository.save(period).getId();

    amount = 1000;
  }

  IdealStockAmount generateInstance() {
    return new IdealStockAmount(facility, commodityType, period, amount);
  }

  @Test
  public void shouldGetListOfIdealStockAmounts() {
    IdealStockAmount isa = generateInstance();

    List<IdealStockAmount> list = isaRepository.search(Collections.singletonList(isa));
    assertThat(list, hasSize(0));

    isa = isaRepository.save(isa);

    list = isaRepository.search(Collections.singletonList(isa));

    assertThat(list, hasSize(1));

    IdealStockAmount idealStockAmount = list.get(0);
    assertThat(idealStockAmount.getAmount(), equalTo(1000));
    assertThat(idealStockAmount.getId(), equalTo(isa.getId()));

    Facility facility = idealStockAmount.getFacility();
    assertThat(facility, is(notNullValue()));
    assertThat(facility.getId(), equalTo(this.facility.getId()));

    ProcessingPeriod processingPeriod = idealStockAmount.getProcessingPeriod();
    assertThat(processingPeriod, is(notNullValue()));
    assertThat(processingPeriod.getId(), equalTo(period.getId()));
    assertThat(processingPeriod.getName(), equalTo(period.getName()));

    ProcessingSchedule processingSchedule = processingPeriod.getProcessingSchedule();
    assertThat(processingSchedule, is(notNullValue()));
    assertThat(processingSchedule.getCode(), equalTo(schedule.getCode()));

    CommodityType commodityType = idealStockAmount.getCommodityType();
    assertThat(commodityType, is(notNullValue()));
    assertThat(commodityType.getId(), equalTo(this.commodityType.getId()));
    assertThat(
        commodityType.getClassificationId(),
        equalTo(this.commodityType.getClassificationId())
    );
    assertThat(
        commodityType.getClassificationSystem(),
        equalTo(this.commodityType.getClassificationSystem())
    );
  }

  @Test
  public void shouldGetPageOfIdealStockAmounts() {
    isaRepository.save(generateInstance());
    IdealStockAmount isa = isaRepository.save(generateInstance());

    Page<IdealStockAmount> page = isaRepository
        .search(facilityId, commodityTypeId, processingPeriodId, new PageRequest(1, 1));

    checkPageProperties(page);
    checkIsaProperties(isa, page);
  }

  @Test
  public void shouldGetPageOfIdealStockAmountsWhenAnyParameterIsNull() {
    isaRepository.save(generateInstance());

    PageRequest pageable = new PageRequest(0, 10);
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

    PageRequest pageable = new PageRequest(0, 10);
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
        .search(null, null, null, null);

    assertEquals(0, page.getContent().size());
  }

  @Test
  public void shouldGetAllIdealStockAmountsIfNoParamsProvided() {
    isaRepository.save(generateInstance());
    isaRepository.save(generateInstance());
    isaRepository.save(generateInstance());

    Page<IdealStockAmount> page = isaRepository
        .search(null, null, null, new PageRequest(0, 10));

    assertEquals(3, page.getContent().size());
  }

  @Test
  public void shouldGetEmptyPageOfIdealStockAmountsIfMatchingIsaNotFound() {
    isaRepository.save(generateInstance());

    Page<IdealStockAmount> page = isaRepository
        .search(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new PageRequest(0, 10));

    assertEquals(0, page.getContent().size());
  }

  private void checkIsaProperties(IdealStockAmount isa, Page<IdealStockAmount> page) {
    IdealStockAmount idealStockAmount = page.getContent().get(0);
    assertEquals(isa.getId(), idealStockAmount.getId());
    assertEquals(amount, idealStockAmount.getAmount());
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
