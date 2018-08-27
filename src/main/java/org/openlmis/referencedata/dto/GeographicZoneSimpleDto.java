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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class GeographicZoneSimpleDto extends BaseDto implements
    GeographicZone.Exporter, GeographicZone.Importer {
  private String code;
  private String name;
  private GeographicLevelDto level;
  private Integer catchmentPopulation;
  private Double latitude;
  private Double longitude;
  private GeographicZoneSimpleDto parent;

  @Override
  public void setLevel(GeographicLevel level) {
    this.level = new GeographicLevelDto();
    level.export(this.level);
  }

  @Override
  public void setParent(GeographicZone parent) {
    this.parent = new GeographicZoneSimpleDto();
    parent.export(this.parent);
  }
  
  @Override
  public void setBoundary(Polygon boundary) {}
  
  @Override
  public Polygon getBoundary() {
    return null;
  }
}
