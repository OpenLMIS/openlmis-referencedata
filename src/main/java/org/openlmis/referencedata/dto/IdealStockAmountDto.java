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

import lombok.Getter;
import lombok.Setter;
import org.openlmis.referencedata.domain.CommodityType;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.IdealStockAmount;
import org.openlmis.referencedata.domain.ProcessingPeriod;

@Getter
@Setter
public class IdealStockAmountDto extends BaseDto implements IdealStockAmount.Exporter {

  private ObjectReferenceDto facility;
  private ObjectReferenceDto commodityType;
  private ObjectReferenceDto processingPeriod;
  private Integer amount;

  public void setFacility(Facility facility) {
    this.facility = new ObjectReferenceDto("api/facilities", facility.getId());
  }

  public void setCommodityType(CommodityType commodityType) {
    this.commodityType = new ObjectReferenceDto("api/commodityTypes", commodityType.getId());
  }

  public void setProcessingPeriod(ProcessingPeriod processingPeriod) {
    this.processingPeriod = new ObjectReferenceDto("api/processingPeriods",
        processingPeriod.getId());
  }
}
