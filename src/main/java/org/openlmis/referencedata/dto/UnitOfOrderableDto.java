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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.referencedata.domain.UnitOfOrderable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UnitOfOrderableDto extends BaseDto
    implements UnitOfOrderable.Exporter, UnitOfOrderable.Importer {
  private String name;

  private String description;

  private Integer displayOrder;

  private Integer factor;

  /**
   * Creates new instance of UnitOfOrderableDto.
   *
   * @param unit the Unit Of Orderable, not null
   * @return new instance of Unit Of Orderable Dto, never null
   */
  public static UnitOfOrderableDto newInstance(UnitOfOrderable unit) {
    UnitOfOrderableDto dto = new UnitOfOrderableDto();
    unit.export(dto);
    return dto;
  }

  /**
   * Creates a list of new instances of UnitOfOrderableDto.
   *
   * @param units the list of units, not null
   * @return the list of new instances, never null
   */
  public static List<UnitOfOrderableDto> newInstances(List<UnitOfOrderable> units) {
    return units != null
        ? units.stream().map(UnitOfOrderableDto::newInstance).collect(Collectors.toList())
        : Collections.emptyList();
  }
}
