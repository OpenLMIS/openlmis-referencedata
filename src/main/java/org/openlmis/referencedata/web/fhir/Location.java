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

package org.openlmis.referencedata.web.fhir;

import static org.openlmis.referencedata.web.fhir.Coding.AREA;

import com.google.common.collect.ImmutableList;

import org.openlmis.referencedata.domain.GeographicZone;

import lombok.Getter;

import java.util.List;

@Getter
public final class Location extends Resource {
  private final List<String> alias;
  private final List<Identifier> identifier;
  private final String name;
  private final Position position;
  private final PhysicalType physicalType;
  private final Reference partOf;

  /**
   * Creates new instance of Location based on data from {@link GeographicZone}.
   */
  Location(String serviceUrl, GeographicZone zone) {
    super(zone.getId(), "Location");

    this.alias = ImmutableList.of(zone.getCode());
    this.identifier = ImmutableList.of(new Identifier(serviceUrl, zone.getLevel()));
    this.name = zone.getName();
    this.position = new Position(zone.getLongitude(), zone.getLatitude());
    this.physicalType = new PhysicalType(AREA);
    this.partOf = null == zone.getParent()
        ? null
        : new Reference(serviceUrl, "api/geographicZones", zone.getParent().getId());
  }

}
