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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.repository.IdealStockAmountRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class IdealStockAmountWriterTest {

  @Mock
  private IdealStockAmountRepository idealStockAmountRepository;

  @InjectMocks
  private IdealStockAmountWriter idealStockAmountWriter;

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

    BaseEntity entity = idealStockAmountWriter.getExisting(idealStockAmount);

    assertEquals(existingCatalogItem.getId(), entity.getId());
  }


  @Test
  public void shouldReturnIfExistingItemNotFound() {
    when(idealStockAmountRepository.existsByFacilityAndCommodityTypeAndProcessingPeriod(facility,
        commodityType, processingPeriod)).thenReturn(false);

    BaseEntity entity = idealStockAmountWriter.getExisting(idealStockAmount);

    assertEquals(null, entity);
  }
}
