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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.Dispensable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DispensableDto implements Dispensable.Exporter, Dispensable.Importer {

  private String dispensingUnit;

  /**
   * Creates new instance based on given {@link Dispensable}.
   *
   * @param dispensable instance of Dispensable.
   * @return new instance of DispensableDto.
   */
  public static DispensableDto newInstance(Dispensable dispensable) {
    if (dispensable == null) {
      return null;
    }
    DispensableDto dispensableDto = new DispensableDto();
    dispensable.export(dispensableDto);

    return dispensableDto;

  }
}
