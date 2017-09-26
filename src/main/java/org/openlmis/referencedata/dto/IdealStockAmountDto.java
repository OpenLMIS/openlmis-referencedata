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

package org.openlmis.referencedata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.web.csv.model.ImportField;

import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.COMMODITY_TYPE;
import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.FACILITY_TYPE;
import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.PROCESSING_PERIOD_TYPE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IdealStockAmountDto extends BaseDto
    implements IdealStockAmount.Exporter, IdealStockAmount.Importer {

  public static final String FACILITY_CODE = "Facility Code";
  public static final String COMMODITY = "Commodity Type";
  public static final String PERIOD = "Period";
  public static final String IDEAL_STOCK_AMOUNT = "Ideal Stock Amount";

  @ImportField(name = FACILITY_CODE, type = FACILITY_TYPE, mandatory = true)
  private BasicFacilityDto facility;

  @ImportField(name = COMMODITY, type = COMMODITY_TYPE, mandatory = true)
  private CommodityTypeDto commodityType;

  @ImportField(name = PERIOD, type = PROCESSING_PERIOD_TYPE, mandatory = true)
  private ProcessingPeriodDto processingPeriod;

  @ImportField(name = IDEAL_STOCK_AMOUNT, mandatory = true)
  private Integer amount;

  @Override
  public void setFacility(Facility facility) {
    this.facility = new BasicFacilityDto();
    facility.export(this.facility);
  }

  @Override
  public void setCommodityType(CommodityType commodityType) {
    this.commodityType = new CommodityTypeDto();
    commodityType.export(this.commodityType);
  }

  @Override
  public void setProcessingPeriod(ProcessingPeriod processingPeriod) {
    this.processingPeriod = new ProcessingPeriodDto();
    processingPeriod.export(this.processingPeriod);
  }
}
