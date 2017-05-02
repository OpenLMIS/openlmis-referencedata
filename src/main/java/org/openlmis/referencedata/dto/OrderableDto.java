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
import org.openlmis.referencedata.domain.Orderable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"identifiers"})
public class OrderableDto extends BaseDto implements Orderable.Importer, Orderable.Exporter {

  private String productCode;

  private String dispensable;

  private String fullProductName;

  private long netContent;

  private long packRoundingThreshold;

  private boolean roundToZero;

  private Set<ProgramOrderableDto> programOrderables;

  private Map<String, String> identifiers;

  /**
   * Create new set of OrderableDto based on given iterable of {@link Orderable}
   *
   * @param orderables list of {@link Orderable}
   * @return new list of OrderableDto.
   */
  public static Set<OrderableDto> newInstance(Iterable<Orderable> orderables) {
    Set<OrderableDto> orderableDtos = new HashSet<>();
    orderables.forEach(oe -> orderableDtos.add(newInstance(oe)));
    return orderableDtos;
  }

  /**
   * Creates new instance based on given {@link Orderable}.
   *
   * @param po instance of Orderable.
   * @return new instance of OrderableDto.
   */
  public static OrderableDto newInstance(Orderable po) {
    if (po == null) {
      return null;
    }
    OrderableDto orderableDto = new OrderableDto();
    po.export(orderableDto);

    return orderableDto;
  }
}
