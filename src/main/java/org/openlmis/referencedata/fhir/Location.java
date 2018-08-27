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

package org.openlmis.referencedata.fhir;

import static org.apache.commons.lang.BooleanUtils.isTrue;
import static org.openlmis.referencedata.fhir.Coding.AREA;
import static org.openlmis.referencedata.fhir.Coding.SITE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.collections.CollectionUtils;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.SupportedProgram;
import org.openlmis.referencedata.web.FacilityOperatorController;
import org.openlmis.referencedata.web.FacilityTypeController;
import org.openlmis.referencedata.web.GeographicLevelController;
import org.openlmis.referencedata.web.LocationController;
import org.openlmis.referencedata.web.ProgramController;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Location extends Resource {

  static final String RESOURCE_TYPE_NAME = "Location";

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
    super(id, RESOURCE_TYPE_NAME);

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

    if (!CollectionUtils.isEmpty(supportedPrograms)) {
      identifier = new ArrayList<>(supportedPrograms.size() + 2);
      supportedPrograms.forEach(sp -> identifier.add(new Identifier(
          serviceUrl, ProgramController.RESOURCE_PATH, sp.programId())));
    } else {
      identifier = new ArrayList<>(2);
    }

    identifier.add(new Identifier(
        serviceUrl, FacilityTypeController.RESOURCE_PATH, facility.getType().getId()));

    Optional
        .ofNullable(facility.getOperator())
        .ifPresent(operator -> identifier.add(new Identifier(
            serviceUrl, FacilityOperatorController.RESOURCE_PATH, operator.getId())));

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
    return isTrue(facility.getActive()) ? Status.ACTIVE.toString() : Status.INACTIVE.toString();
  }

}
