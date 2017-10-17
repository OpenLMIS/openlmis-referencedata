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

import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.dto.IdealStockAmountCsvModel;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.CommodityTypeRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.IdealStockAmountRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.web.csv.parser.CsvParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_COMMODITY_TYPE_NOT_FOUND;
import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_FACILITY_NOT_FOUND;
import static org.openlmis.referencedata.util.messagekeys.IdealStockAmountMessageKeys.ERROR_PROCESSING_PERIOD_NOT_FOUND;

/**
 * IdealStockAmountProcessor is used for uploads of Ideal Stock Amount.
 * It uploads each ideal stock amount by record.
 */
@Component
public class IdealStockAmountProcessor
    implements RecordProcessor<IdealStockAmountCsvModel, IdealStockAmount> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdealStockAmountProcessor.class);

  @Autowired
  private IdealStockAmountRepository idealStockAmountRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private ProcessingPeriodRepository processingPeriodRepository;

  @Autowired
  private CommodityTypeRepository commodityTypeRepository;

  @Override
  public List<IdealStockAmount> process(List<IdealStockAmountCsvModel> records) {
    Profiler profiler = new Profiler("PROCESS_DTO_CHUNK");
    profiler.setLogger(LOGGER);

    profiler.start("SEARCH_EXISTING_ISA");
    List<IdealStockAmount> idealStockAmounts = idealStockAmountRepository.search(convert(records));

    List<IdealStockAmount> resultList = new ArrayList<>();

    profiler.start("PROCESS_RECORDS");
    for (IdealStockAmountCsvModel isa : records) {
      IdealStockAmount result = getExisting(idealStockAmounts, isa);
      if (null == result) {
        resultList.add(prepareNewIdealStockAmountObject(isa));
      } else {
        result.setAmount(isa.getAmount());
        resultList.add(result);
      }
    }

    profiler.stop().log();

    return resultList;
  }

  private IdealStockAmount getExisting(List<IdealStockAmount> list,
                                       IdealStockAmountCsvModel record) {
    for (IdealStockAmount isa : list) {
      if (isa.getFacility().getCode().equals(record.getFacility().getCode())
          && isa.getProcessingPeriod().getName().equals(record.getProcessingPeriod().getName())
          && isa.getProcessingPeriod().getProcessingSchedule().getCode().equals(
              record.getProcessingPeriod().getProcessingSchedule().getCode())
          && isa.getCommodityType().getClassificationId().equals(
              record.getCommodityType().getClassificationId())
          && isa.getCommodityType().getClassificationSystem().equals(
              record.getCommodityType().getClassificationSystem())) {
        return isa;
      }
    }
    return null;
  }

  private IdealStockAmount prepareNewIdealStockAmountObject(IdealStockAmountCsvModel isa) {
    Facility facility = facilityRepository.findByCode(isa.getFacility().getCode())
        .orElseThrow(() -> new ValidationMessageException(new Message(ERROR_FACILITY_NOT_FOUND,
            isa.getFacility().getCode())));

    ProcessingPeriod period = processingPeriodRepository.findByNameAndProcessingScheduleCode(
        isa.getProcessingPeriod().getName(),
        isa.getProcessingPeriod().getProcessingSchedule().getCode())
        .orElseThrow(() -> new ValidationMessageException(
            new Message(ERROR_PROCESSING_PERIOD_NOT_FOUND,
            isa.getProcessingPeriod().getName(),
            isa.getProcessingPeriod().getProcessingSchedule().getCode())));

    CommodityType commodityType = commodityTypeRepository
        .findByClassificationIdAndClassificationSystem(isa.getCommodityType().getClassificationId(),
            isa.getCommodityType().getClassificationSystem())
        .orElseThrow(() -> new ValidationMessageException(new Message(
            ERROR_COMMODITY_TYPE_NOT_FOUND,
            isa.getCommodityType().getClassificationId(),
            isa.getCommodityType().getClassificationSystem())));

    return new IdealStockAmount(facility, commodityType, period, isa.getAmount());
  }

  private List<IdealStockAmount> convert(List<IdealStockAmountCsvModel> list) {
    List<IdealStockAmount> result = new ArrayList<>();

    for (IdealStockAmountCsvModel isa : list) {
      final Facility facility = new Facility(isa.getFacility().getCode());

      ProcessingSchedule schedule = new ProcessingSchedule();
      schedule.setCode(isa.getProcessingPeriod().getProcessingSchedule().getCode());
      ProcessingPeriod period = new ProcessingPeriod();
      period.setName(isa.getProcessingPeriod().getName());
      period.setProcessingSchedule(schedule);

      CommodityType commodityType = new CommodityType();
      commodityType.setClassificationId(isa.getCommodityType().getClassificationId());
      commodityType.setClassificationSystem(isa.getCommodityType().getClassificationSystem());

      result.add(new IdealStockAmount(facility, commodityType, period, isa.getAmount()));
    }

    return result;
  }
}