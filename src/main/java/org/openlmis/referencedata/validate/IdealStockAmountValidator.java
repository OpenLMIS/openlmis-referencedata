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

import org.openlmis.referencedata.dto.IdealStockAmountCsvModel;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_COMMODITY_TYPE_NOT_FOUND;
import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_FACILITY_NOT_FOUND;
import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_FROM_FIELD_REQUIRED;
import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_PROCESSING_PERIOD_NOT_FOUND;
import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_PROCESSING_SCHEDULE_NOT_FOUND;

@Component
public class IdealStockAmountValidator {

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private CommodityTypeRepository commodityTypeRepository;

  @Autowired
  private ProcessingPeriodRepository processingPeriodRepository;

  @Autowired
  private ProcessingScheduleRepository processingScheduleRepository;

  public void validate(IdealStockAmountCsvModel idealStockAmount) {
    validateNullValues(idealStockAmount);
    validateNestedObjectsExist(idealStockAmount);
  }

  private void validateNestedObjectsExist(IdealStockAmountCsvModel idealStockAmount) {
    if (!facilityRepository.existsByCode(idealStockAmount.getFacility().getCode())) {
      throw new ValidationMessageException(new Message(ERROR_FACILITY_NOT_FOUND,
          idealStockAmount.getFacility().getCode()));
    }

    if (!processingScheduleRepository.existsByCode(
        idealStockAmount.getProcessingPeriod().getProcessingSchedule().getCode())) {
      throw new ValidationMessageException(new Message(ERROR_PROCESSING_SCHEDULE_NOT_FOUND,
          idealStockAmount.getProcessingPeriod().getProcessingSchedule().getCode()));
    }

    if (!processingPeriodRepository.existsByNameAndProcessingSchedule(
        idealStockAmount.getProcessingPeriod().getName(),
        idealStockAmount.getProcessingPeriod().getProcessingSchedule())) {
      throw new ValidationMessageException(new Message(ERROR_PROCESSING_PERIOD_NOT_FOUND,
          idealStockAmount.getProcessingPeriod().getName()));
    }

    if (!commodityTypeRepository.existsByClassificationIdAndClassificationSystem(
        idealStockAmount.getCommodityType().getClassificationId(),
        idealStockAmount.getCommodityType().getClassificationSystem())) {
      throw new ValidationMessageException(new Message(ERROR_COMMODITY_TYPE_NOT_FOUND,
          idealStockAmount.getCommodityType().getClassificationId(),
          idealStockAmount.getCommodityType().getClassificationSystem()));
    }
  }

  private void validateNullValues(IdealStockAmountCsvModel idealStockAmount) {
    validateNotNull(idealStockAmount.getFacility(), ERROR_FROM_FIELD_REQUIRED, "facility");
    validateNotNull(idealStockAmount.getCommodityType(), ERROR_FROM_FIELD_REQUIRED,
        "commodityType");
    validateNotNull(idealStockAmount.getProcessingPeriod(), ERROR_FROM_FIELD_REQUIRED,
        "processingPeriod");
    validateNotNull(idealStockAmount.getProcessingPeriod().getProcessingSchedule(),
        ERROR_FROM_FIELD_REQUIRED, "processingPeriod.processingSchedule");
    validateNotNull(idealStockAmount.getAmount(), ERROR_FROM_FIELD_REQUIRED, "amount");
  }

  private void validateNotNull(Object field, String errorMessage, String fieldName) {
    if (field == null) {
      throw new ValidationMessageException(new Message(errorMessage, fieldName));
    }
  }
}
