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

package org.openlmis.referencedata.testbuilder;

import com.vividsolutions.jts.geom.Polygon;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import java.util.UUID;

public class GeographicZoneDataBuilder {

  private static int instanceNumber = 0;

  private UUID id;
  private String code;
  private String name;
  private GeographicLevel level;
  private GeographicZone parent;
  private Integer catchmentPopulation;
  private Double latitude;
  private Double longitude;
  private Polygon boundary;

  /**
   * Returns instance of {@link GeographicZoneDataBuilder} with sample data.
   */
  public GeographicZoneDataBuilder() {
    instanceNumber++;

    id = UUID.randomUUID();
    code = "GZ" + instanceNumber;
    name = "Geographic Zone " + instanceNumber;
    level = new GeographicLevelDataBuilder().build();
  }

  /**
   * Builds instance of {@link GeographicZone}.
   */
  public GeographicZone build() {
    GeographicZone zone = new GeographicZone(code, name, level, parent, catchmentPopulation,
        latitude, longitude, boundary);
    zone.setId(id);

    return zone;
  }
}
