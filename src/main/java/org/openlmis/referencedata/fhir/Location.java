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

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.openlmis.referencedata.fhir.Coding.AREA;
import static org.openlmis.referencedata.fhir.Coding.SITE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.GeographicZone;
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

  private String name;
  private Position position;
  private PhysicalType physicalType;
  private Reference partOf;
  private String description;
  private String status;

  private Location(UUID id) {
    super(id, RESOURCE_TYPE_NAME);

    this.alias = Lists.newArrayList();
    this.identifier = Lists.newArrayList();
  }

  /**
   * Creates new instance of Location based on data from {@link GeographicZone}.
   */
  static Location newInstance(String serviceUrl, GeographicZone zone) {
    Location location = new Location(zone.getId());
    location.addAlias(zone.getCode());
    location.addIdentifier(
        serviceUrl, GeographicLevelController.RESOURCE_PATH, zone.getLevel().getId());
    location.name = zone.getName();
    location.position = new Position(zone.getLongitude(), zone.getLatitude());
    location.physicalType = new PhysicalType(AREA);

    Optional
        .ofNullable(zone.getParent())
        .ifPresent(parent ->
            location.partOf =
                new Reference(serviceUrl, LocationController.RESOURCE_PATH, parent.getId()));

    return location;
  }

  /**
   * Creates new instance of Location based on data from {@link Facility}.
   */
  static Location newInstance(String serviceUrl, Facility facility) {
    Location location = new Location(facility.getId());
    location.addAlias(facility.getCode());

    Optional
        .ofNullable(facility.getSupportedPrograms())
        .orElse(Collections.emptySet())
        .forEach(sp -> location.addIdentifier(
            serviceUrl, ProgramController.RESOURCE_PATH, sp.programId()));

    location.addIdentifier(
        serviceUrl, FacilityTypeController.RESOURCE_PATH, facility.getType().getId());

    Optional
        .ofNullable(facility.getOperator())
        .ifPresent(operator -> location.addIdentifier(
            serviceUrl, FacilityOperatorController.RESOURCE_PATH, operator.getId()));

    location.name = facility.getName();

    Optional
        .ofNullable(facility.getLocation())
        .ifPresent(point -> location.position = new Position(point.getX(), point.getY()));

    location.physicalType = new PhysicalType(SITE);

    Optional
        .ofNullable(facility.getGeographicZone())
        .ifPresent(zone ->
            location.partOf =
                new Reference(serviceUrl, LocationController.RESOURCE_PATH, zone.getId()));

    location.description = facility.getDescription();

    location.status = isTrue(facility.getActive())
        ? Status.ACTIVE.toString()
        : Status.INACTIVE.toString();

    return location;
  }

  private void addAlias(String alias) {
    this.alias.add(alias);
  }

  private void addIdentifier(String serviceUrl, String path, UUID uuid) {
    identifier.add(new Identifier(serviceUrl, path, uuid));
  }

}
