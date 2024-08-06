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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableChild;
import org.openlmis.referencedata.domain.UnitOfOrderable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OrderableChildDto extends BaseDto
    implements OrderableChild.Exporter, OrderableChild.Importer {

  @Getter
  private ObjectReferenceDto orderable;

  @Getter
  @Setter
  private Long quantity;

  @Getter
  private UnitOfOrderableDto unit;

  /**
   * Create new Set containing OrderableChildDto based on given a set of {@link OrderableChild}.
   *
   * @param children Children.
   * @return a set containing dtos for all orderable children.
   */
  public static Set<OrderableChildDto> newInstance(Set<OrderableChild> children) {
    if (children == null) {
      return Collections.emptySet();
    }
    return children.stream()
        .map(OrderableChildDto::newInstance)
        .collect(Collectors.toSet());
  }

  private static OrderableChildDto newInstance(OrderableChild child) {
    OrderableChildDto dto = new OrderableChildDto();
    child.export(dto);
    return dto;
  }

  @Override
  @JsonIgnore
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  public void setOrderable(Orderable orderable) {
    final ObjectReferenceDto referenceDto = new ObjectReferenceDto();
    referenceDto.setId(orderable.getId());
    setOrderable(referenceDto);
  }

  public void setOrderable(ObjectReferenceDto orderable) {
    this.orderable = orderable;
  }

  @Override
  @JsonIgnore
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  public void setUnit(UnitOfOrderable unit) {
    setUnit(UnitOfOrderableDto.newInstance(unit));
  }

  public void setUnit(UnitOfOrderableDto unit) {
    this.unit = unit;
  }
}
