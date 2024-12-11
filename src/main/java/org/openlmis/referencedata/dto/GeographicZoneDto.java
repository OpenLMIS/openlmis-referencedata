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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vividsolutions.jts.geom.Polygon;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.referencedata.domain.GeographicZone;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
public final class GeographicZoneDto extends GeographicZoneSimpleDto {
  private Polygon boundary;
  private Map<String, Object> extraData;

  /**
   * Create new list of GeographicZoneDto based on given iterable of {@link GeographicZone}.
   *
   * @param geographicZones list of {@link GeographicZoneDto}
   * @return new list of GeographicZoneDto.
   */
  public static List<GeographicZoneDto> newInstances(Iterable<GeographicZone> geographicZones) {
    List<GeographicZoneDto> geographicZoneDtos = new LinkedList<>();
    geographicZones.forEach(gz -> geographicZoneDtos.add(newInstance(gz)));
    return geographicZoneDtos;
  }

  /**
   * Creates new instance of GeographicZoneDto based on given {@link GeographicZone}.
   *
   * @param geographicZone instance of GeographicZone.
   * @return new instance of GeographicZoneDto.
   */
  public static GeographicZoneDto newInstance(GeographicZone geographicZone) {
    if (geographicZone == null) {
      return null;
    }
    GeographicZoneDto geographicZoneDto = new GeographicZoneDto();
    geographicZone.export(geographicZoneDto);

    return geographicZoneDto;
  }
}
