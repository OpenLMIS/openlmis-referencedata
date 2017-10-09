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

package org.openlmis.referencedata.validate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.dto.BasicFacilityDto;
import org.openlmis.referencedata.dto.CommodityTypeDto;
import org.openlmis.referencedata.dto.IdealStockAmountCsvModel;
import org.openlmis.referencedata.dto.ProcessingPeriodDto;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.util.Message;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_COMMODITY_TYPE_NOT_FOUND;
import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_FACILITY_NOT_FOUND;
import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_FROM_FIELD_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_PROCESSING_PERIOD_NOT_FOUND;
import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_PROCESSING_SCHEDULE_NOT_FOUND;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"PMD.TooManyMethods"})
public class IdealStockAmountValidatorTest {

  @Rule
  public final ExpectedException expectedEx = ExpectedException.none();

  @Mock
  private FacilityRepository facilityRepository;

  @Mock
  private CommodityTypeRepository commodityTypeRepository;

  @Mock
  private ProcessingPeriodRepository processingPeriodRepository;

  @Mock
  private ProcessingScheduleRepository processingScheduleRepository;

  @InjectMocks
  private IdealStockAmountsValidator isaValidator;

  private IdealStockAmountCsvModel isa;
  private BasicFacilityDto facility;
  private ProcessingSchedule processingSchedule;
  private ProcessingPeriodDto processingPeriod;
  private CommodityTypeDto commodityType;

  @Before
  public void before() {
    initMocks(this);

    facility = new BasicFacilityDto();
    facility.setCode("facility-code");
    processingSchedule = new ProcessingSchedule();
    processingSchedule.setCode("schedule-code");
    processingPeriod = new ProcessingPeriodDto();
    processingPeriod.setName("period");
    processingPeriod.setProcessingSchedule(processingSchedule);
    commodityType = new CommodityTypeDto();
    commodityType.setClassificationId("classification-id");
    commodityType.setClassificationSystem("classification-system");
    isa = new IdealStockAmountCsvModel(facility, commodityType, processingPeriod, 11);

    when(facilityRepository.existsByCode(facility.getCode())).thenReturn(true);
    when(processingScheduleRepository.existsByCode(
        isa.getProcessingPeriod().getProcessingSchedule().getCode())).thenReturn(true);
    when(processingPeriodRepository.existsByNameAndProcessingSchedule(
        isa.getProcessingPeriod().getName(),
        isa.getProcessingPeriod().getProcessingSchedule())).thenReturn(true);
    when(commodityTypeRepository.existsByClassificationIdAndClassificationSystem(
        isa.getCommodityType().getClassificationId(),
        isa.getCommodityType().getClassificationSystem())).thenReturn(true);
  }

  @Test
  public void shouldNotThrowExceptionIfRequiredFieldsAreNotNull() {
    isaValidator.validate(isa);
  }

  @Test
  public void shouldThrowExceptionIfFacilityIsNull() {
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        new Message(ERROR_FROM_FIELD_REQUIRED, "facility").toString());

    isa.setFacility((BasicFacilityDto) null);
    isaValidator.validate(isa);
  }

  @Test
  public void shouldThrowExceptionIfProcessingPeriodIsNull() {
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        new Message(ERROR_FROM_FIELD_REQUIRED, "processingPeriod").toString());

    isa.setProcessingPeriod((ProcessingPeriodDto) null);
    isaValidator.validate(isa);
  }

  @Test
  public void shouldThrowExceptionIfProcessingScheduleIsNull() {
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        new Message(ERROR_FROM_FIELD_REQUIRED, "processingPeriod.processingSchedule").toString());

    isa.getProcessingPeriod().setProcessingSchedule(null);
    isaValidator.validate(isa);
  }

  @Test
  public void shouldThrowExceptionIfCommodityTypeIsNull() {
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        new Message(ERROR_FROM_FIELD_REQUIRED, "commodityType").toString());

    isa.setCommodityType((CommodityTypeDto) null);
    isaValidator.validate(isa);
  }

  @Test
  public void shouldThrowExceptionIfAmountIsNull() {
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        new Message(ERROR_FROM_FIELD_REQUIRED, "amount").toString());

    isa.setAmount(null);
    isaValidator.validate(isa);
  }

  @Test
  public void shouldThrowExceptionIfFacilityDoesNotExists() {
    when(facilityRepository.existsByCode(isa.getFacility().getCode()))
        .thenReturn(false);

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        new Message(ERROR_FACILITY_NOT_FOUND,
            isa.getFacility().getCode()).toString());

    isaValidator.validate(isa);
  }

  @Test
  public void shouldThrowExceptionIfProcessingPeriodDoesNotExists() {
    when(processingPeriodRepository.existsByNameAndProcessingSchedule(
        isa.getProcessingPeriod().getName(),
        isa.getProcessingPeriod().getProcessingSchedule())).thenReturn(false);

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        new Message(ERROR_PROCESSING_PERIOD_NOT_FOUND,
            isa.getProcessingPeriod().getName()).toString());

    isaValidator.validate(isa);
  }

  @Test
  public void shouldThrowExceptionIfProcessingScheduleDoesNotExists() {
    when(processingScheduleRepository.existsByCode(
        isa.getProcessingPeriod().getProcessingSchedule().getCode())).thenReturn(false);

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        new Message(ERROR_PROCESSING_SCHEDULE_NOT_FOUND,
            isa.getProcessingPeriod().getProcessingSchedule().getCode()).toString());

    isaValidator.validate(isa);
  }

  @Test
  public void shouldThrowExceptionIfCommodityTypeDoesNotExists() {
    when(commodityTypeRepository.existsByClassificationIdAndClassificationSystem(
        isa.getCommodityType().getClassificationId(),
        isa.getCommodityType().getClassificationSystem())).thenReturn(false);

    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(
        new Message(ERROR_COMMODITY_TYPE_NOT_FOUND,
            isa.getCommodityType().getClassificationId(),
            isa.getCommodityType().getClassificationSystem()).toString());

    isaValidator.validate(isa);
  }
}
