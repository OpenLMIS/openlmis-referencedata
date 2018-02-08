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
import static org.openlmis.referencedata.web.fhir.Coding.SITE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;
import lombok.Getter;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.web.FacilityOperatorController;
import org.openlmis.referencedata.web.FacilityTypeController;
import org.openlmis.referencedata.web.GeographicLevelController;
import org.openlmis.referencedata.web.LocationController;
import org.openlmis.referencedata.web.ProgramController;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Location extends Resource {
  private static final String LOCATION = "Location";

  private final List<String> alias;
  private final List<Identifier> identifier;
  private final String name;
  private final Position position;
  private final PhysicalType physicalType;
  private final Reference partOf;
  private final String description;
  private final String status;

  /**
   * Creates new instance of Location based on data from {@link GeographicZone}.
   */
  Location(String serviceUrl, GeographicZone zone) {
    this(zone.getId(), ImmutableList.of(zone.getCode()), getIdentifier(serviceUrl, zone),
        zone.getName(), new Position(zone.getLongitude(), zone.getLatitude()),
        new PhysicalType(AREA), getGeographicZoneAsReference(serviceUrl, zone.getParent()),
        null, null);
  }

  /**
   * Creates new instance of Location based on data from {@link Facility}.
   */
  Location(String serviceUrl, Facility facility) {
    this(facility.getId(), ImmutableList.of(facility.getCode()),
        getIdentifier(serviceUrl, facility), facility.getName(),
        getPosition(facility.getLocation()), new PhysicalType(SITE),
        getGeographicZoneAsReference(serviceUrl, facility.getGeographicZone()),
        facility.getDescription(), getStatus(facility));
  }

  private Location(UUID id, List<String> alias, List<Identifier> identifier, String name,
                   Position position, PhysicalType physicalType, Reference partOf,
                   String description, String status) {
    super(id, LOCATION);

    this.alias = alias;
    this.identifier = identifier;
    this.name = name;
    this.position = position;
    this.physicalType = physicalType;
    this.partOf = partOf;
    this.description = description;
    this.status = status;
  }

  private static List<Identifier> getIdentifier(String serviceUrl, GeographicZone zone) {
    return ImmutableList.of(
        new Identifier(
            serviceUrl,
            GeographicLevelController.RESOURCE_PATH, zone.getLevel().getId()));
  }

  private static List<Identifier> getIdentifier(String serviceUrl, Facility facility) {
    Set<SupportedProgram> supportedPrograms = facility.getSupportedPrograms();
    List<Identifier> identifier;
    if (supportedPrograms != null) {
      identifier = new ArrayList<>(supportedPrograms.size() + 2);
      supportedPrograms.forEach(sp -> identifier.add(new Identifier(
          serviceUrl, ProgramController.RESOURCE_PATH, sp.programId())));
    } else {
      identifier = new ArrayList<>(2);
    }
    identifier.add(new Identifier(
        serviceUrl, FacilityTypeController.RESOURCE_PATH, facility.getType().getId()));
    FacilityOperator operator = facility.getOperator();
    if (operator != null) {
      identifier.add(new Identifier(
          serviceUrl, FacilityOperatorController.RESOURCE_PATH, operator.getId()));
    }
    return identifier;
  }

  private static Reference getGeographicZoneAsReference(String serviceUrl, GeographicZone zone) {
    return zone != null
        ? new Reference(serviceUrl, LocationController.RESOURCE_PATH, zone.getId())
        : null;
  }

  private static Position getPosition(Point location) {
    return location != null
        ? new Position(location.getX(), location.getY())
        : null;
  }

  private static String getStatus(Facility facility) {
    return facility.getActive() ? Status.ACTIVE.toString() : Status.INACTIVE.toString();
  }

}
