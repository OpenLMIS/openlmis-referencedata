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

import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.dto.IdealStockAmountCsvModel;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.IdealStockAmountRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.validate.IdealStockAmountValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * IdealStockAmountsPersistenceHandler is used for uploads of Ideal Stock Amount.
 * It uploads each catalog item record by record.
 */
@Component
public class IdealStockAmountsPersistenceHandler
    extends AbstractPersistenceHandler<IdealStockAmount, IdealStockAmountCsvModel> {

  @Autowired
  private IdealStockAmountValidator idealStockAmountsValidator;

  @Autowired
  private IdealStockAmountRepository idealStockAmountRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private ProcessingPeriodRepository processingPeriodRepository;

  @Autowired
  private ProcessingScheduleRepository processingScheduleRepository;

  @Autowired
  private CommodityTypeRepository commodityTypeRepository;

  @Override
  protected IdealStockAmount getExisting(IdealStockAmount record) {
    if (idealStockAmountRepository.existsByFacilityAndCommodityTypeAndProcessingPeriod(
        record.getFacility(), record.getCommodityType(), record.getProcessingPeriod())) {
      return idealStockAmountRepository.findByFacilityAndCommodityTypeAndProcessingPeriod(
          record.getFacility(), record.getCommodityType(), record.getProcessingPeriod());
    }
    return null;
  }

  @Override
  protected IdealStockAmount importDto(IdealStockAmountCsvModel record) {
    idealStockAmountsValidator.validate(record);
    facilityRepository.findFirstByCode(record.getFacility().getCode()).export(record.getFacility());
    ProcessingSchedule schedule = processingScheduleRepository
        .findByCode(record.getProcessingPeriod().getProcessingSchedule().getCode());
    processingPeriodRepository.findByNameAndProcessingSchedule(
        record.getProcessingPeriod().getName(), schedule).export(record.getProcessingPeriod());
    commodityTypeRepository.findByClassificationIdAndClassificationSystem(
        record.getCommodityType().getClassificationId(),
        record.getCommodityType().getClassificationSystem()).export(record.getCommodityType());
    return IdealStockAmount.newIdealStockAmount(record);
  }

  @Override
  protected void save(IdealStockAmount record) {
    idealStockAmountRepository.save(record);
  }

}