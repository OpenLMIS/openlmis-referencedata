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

package org.openlmis.referencedata.web.csv.recordhandler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.dto.BasicFacilityDto;
import org.openlmis.referencedata.dto.CommodityTypeDto;
import org.openlmis.referencedata.dto.IdealStockAmountCsvModel;
import org.openlmis.referencedata.dto.ProcessingPeriodDto;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.IdealStockAmountRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.validate.IdealStockAmountsValidator;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IdealStockAmountPersistenceHandlerTest {

  @Captor
  private ArgumentCaptor<IdealStockAmount> idealStockAmountArgumentCaptor;

  @Mock
  private IdealStockAmountRepository idealStockAmountRepository;

  @Mock
  private FacilityRepository facilityRepository;

  @Mock
  private ProcessingPeriodRepository processingPeriodRepository;

  @Mock
  private ProcessingScheduleRepository processingScheduleRepository;

  @Mock
  private CommodityTypeRepository commodityTypeRepository;

  @Mock
  private IdealStockAmountsValidator idealStockAmountsValidator;

  @InjectMocks
  private IdealStockAmountsPersistenceHandler idealStockAmountsPersistenceHandler;

  private IdealStockAmount idealStockAmount;
  private Facility facility;
  private CommodityType commodityType;
  private ProcessingPeriod processingPeriod;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    facility = new Facility("facility-code");
    commodityType = new CommodityType();
    processingPeriod = new ProcessingPeriod();
    processingPeriod.setStartDate(LocalDate.of(2017, 10, 1));
    processingPeriod.setEndDate(LocalDate.of(2017, 10, 30));

    idealStockAmount = new IdealStockAmount();
    idealStockAmount.setFacility(facility);
    idealStockAmount.setCommodityType(commodityType);
    idealStockAmount.setProcessingPeriod(processingPeriod);
  }

  @Test
  public void shouldSetIdIfExistingItemFoundByCode() {
    IdealStockAmount existingCatalogItem = new IdealStockAmount();
    existingCatalogItem.setId(UUID.randomUUID());

    when(idealStockAmountRepository.existsByFacilityAndCommodityTypeAndProcessingPeriod(facility,
        commodityType, processingPeriod)).thenReturn(true);
    when(idealStockAmountRepository.findByFacilityAndCommodityTypeAndProcessingPeriod(facility,
        commodityType, processingPeriod)).thenReturn(existingCatalogItem);

    idealStockAmountsPersistenceHandler.execute(idealStockAmount);

    verify(idealStockAmountRepository).save(idealStockAmountArgumentCaptor.capture());
    assertEquals(existingCatalogItem.getId(), idealStockAmountArgumentCaptor.getValue().getId());
  }

  @Test
  public void shouldNotSetIdIfExistingItemNotFound() {
    when(idealStockAmountRepository.existsByFacilityAndCommodityTypeAndProcessingPeriod(facility,
        commodityType, processingPeriod)).thenReturn(false);

    idealStockAmountsPersistenceHandler.execute(idealStockAmount);

    verify(idealStockAmountRepository).save(idealStockAmount);
  }

  @Test
  public void shouldSetFullObjectsToIdealStockAmountsModel() {
    BasicFacilityDto facilityDto = new BasicFacilityDto();
    facilityDto.setCode("facility-code");
    CommodityTypeDto commodityTypeDto = new CommodityTypeDto();
    commodityTypeDto.setClassificationSystem("system");
    commodityTypeDto.setClassificationId("id");
    ProcessingSchedule schedule = new ProcessingSchedule();
    schedule.setCode("schedule");
    ProcessingPeriodDto processingPeriodDto = new ProcessingPeriodDto();
    processingPeriodDto.setName("period");
    processingPeriodDto.setProcessingSchedule(schedule);

    IdealStockAmountCsvModel isa = new IdealStockAmountCsvModel(facilityDto, commodityTypeDto,
        processingPeriodDto, 1212);

    idealStockAmountsValidator.validate(isa);
    when(facilityRepository.findFirstByCode("facility-code")).thenReturn(facility);
    when(processingScheduleRepository.findByCode("schedule")).thenReturn(schedule);
    when(processingPeriodRepository.findByNameAndProcessingSchedule("period", schedule))
        .thenReturn(processingPeriod);
    when(commodityTypeRepository.findByClassificationIdAndClassificationSystem("id",  "system"))
        .thenReturn(commodityType);

    IdealStockAmount idealStockAmount = idealStockAmountsPersistenceHandler.importDto(isa);

    assertEquals(idealStockAmount.getFacility(), facility);
    assertEquals(idealStockAmount.getAmount(), new Integer(1212));
    assertEquals(idealStockAmount.getCommodityType(), commodityType);
    assertEquals(idealStockAmount.getFacility(), facility);
  }
}