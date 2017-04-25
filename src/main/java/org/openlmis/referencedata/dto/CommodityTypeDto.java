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
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class CommodityTypeDto extends BaseDto
    implements CommodityType.Importer, CommodityType.Exporter {

  /**
   * Create new list of CommodityTypeDto based on given list of {@link CommodityType}
   *
   * @param tradeItems list of {@link CommodityType}
   * @return new list of CommodityTypeDto.
   */
  public static Set<CommodityTypeDto> newInstance(Iterable<CommodityType> tradeItems) {
    Set<CommodityTypeDto> commodityTypeDtos = new HashSet<>();
    tradeItems.forEach(oe -> commodityTypeDtos.add(newInstance(oe)));
    return commodityTypeDtos;
  }


  /**
   * Creates new instance based on given {@link CommodityType}.
   *
   * @param ct instance of CommodityType.
   * @return new instance of CommodityTypeDto.
   */
  public static CommodityTypeDto newInstance(CommodityType ct) {
    if (ct == null) {
      return null;
    }
    CommodityTypeDto commodityTypeDto = new CommodityTypeDto();
    //ct.export(commodityTypeDto);

    return commodityTypeDto;
  }

}
