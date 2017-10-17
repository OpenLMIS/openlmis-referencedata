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
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    facilityRepository.save(facility);

    commodityType = new CommodityType("Name", "cSys", "cId", null, new ArrayList<>());
    commodityTypeRepository.save(commodityType);

    schedule = new ProcessingSchedule();
    schedule.setCode("schedule-code");
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
    processingPeriodRepository.save(period);
  }

  IdealStockAmount generateInstance() {
    return new IdealStockAmount(facility, commodityType, period, 1000);
  }

  @Test
  public void shouldGetListOfIdealStockAmounts() {
    IdealStockAmount isa = generateInstance();

    assertTrue(isaRepository.search(Collections.singletonList(isa)).isEmpty());

    isa = isaRepository.save(isa);

    assertEquals(1, isaRepository.search(Collections.singletonList(isa)).size());
    assertEquals(isa, isaRepository.search(Collections.singletonList(isa)).get(0));
  }
}
