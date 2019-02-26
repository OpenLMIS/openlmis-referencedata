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

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MinimalOrderableDto extends BaseDto implements Orderable.Exporter {

  @Getter
  @Setter
  private String productCode;

  @Getter
  @Setter
  private String fullProductName;

  /**
   * Creates a new Minimal Orderable DTO.
   *
   * @param orderable Orderable
   * @return MinimalOrderableDto
   */
  public static MinimalOrderableDto newInstance(Orderable orderable) {
    MinimalOrderableDto dto = new MinimalOrderableDto();
    orderable.export(dto);
    return dto;
  }

  @Override
  public void setDispensable(Dispensable dispensable) {
    // unsupported operation
  }


  @Override
  public void setDescription(String description) {
    // unsupported operation
  }

  @Override
  public void setNetContent(Long netContent) {
    // unsupported operation
  }

  @Override
  public void setPackRoundingThreshold(Long packRoundingThreshold) {
    // unsupported operation
  }

  @Override
  public void setRoundToZero(Boolean roundToZero) {
    // unsupported operation
  }

  @Override
  public void setPrograms(Set<ProgramOrderableDto> programOrderables) {
    // unsupported operation
  }

  @Override
  public void setChildren(Set<OrderableChildDto> children) {
    // unsupported operation
  }

  @Override
  public void setIdentifiers(Map<String, String> identifiers) {
    // unsupported operation
  }

  @Override
  public void setVersionId(Long versionId) {
    // unsupported operation
  }

  @Override
  public void setLastUpdated(ZonedDateTime lastUpdated) {
    // unsupported operation
  }

  @Override
  public void setExtraData(Map<String, Object> extraData) {
    // unsupported operation
  }
}
