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
import static org.openlmis.referencedata.fhir.FhirCoding.AREA;
import static org.openlmis.referencedata.fhir.FhirCoding.SITE;

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
public final class FhirLocation extends FhirResource {
  static final String RESOURCE_TYPE_NAME = "Location";

  private final List<String> alias;
  private final List<FhirIdentifier> identifier;

  private String name;
  private FhirPosition position;
  private FhirPhysicalType physicalType;
  private FhirReference partOf;
  private String description;
  private String status;

  private FhirLocation(UUID id) {
    super(id, RESOURCE_TYPE_NAME);

    this.alias = Lists.newArrayList();
    this.identifier = Lists.newArrayList();
  }

  /**
   * Creates new instance of FHIR Location based on data from {@link GeographicZone}.
   */
  static FhirLocation newInstance(String serviceUrl, GeographicZone zone) {
    FhirLocation fhirLocation = new FhirLocation(zone.getId());
    fhirLocation.addAlias(zone.getCode());
    fhirLocation.addIdentifier(
        serviceUrl, GeographicLevelController.RESOURCE_PATH, zone.getLevel().getId());
    fhirLocation.name = zone.getName();
    fhirLocation.position = new FhirPosition(zone.getLongitude(), zone.getLatitude());
    fhirLocation.physicalType = new FhirPhysicalType(AREA);

    Optional
        .ofNullable(zone.getParent())
        .ifPresent(parent ->
            fhirLocation.partOf =
                new FhirReference(serviceUrl, LocationController.RESOURCE_PATH, parent.getId()));

    return fhirLocation;
  }

  /**
   * Creates new instance of FHIR Location based on data from {@link Facility}.
   */
  static FhirLocation newInstance(String serviceUrl, Facility facility) {
    FhirLocation fhirLocation = new FhirLocation(facility.getId());
    fhirLocation.addAlias(facility.getCode());

    Optional
        .ofNullable(facility.getSupportedPrograms())
        .orElse(Collections.emptySet())
        .forEach(sp -> fhirLocation.addIdentifier(
            serviceUrl, ProgramController.RESOURCE_PATH, sp.programId()));

    fhirLocation.addIdentifier(
        serviceUrl, FacilityTypeController.RESOURCE_PATH, facility.getType().getId());

    Optional
        .ofNullable(facility.getOperator())
        .ifPresent(operator -> fhirLocation.addIdentifier(
            serviceUrl, FacilityOperatorController.RESOURCE_PATH, operator.getId()));

    fhirLocation.name = facility.getName();

    Optional
        .ofNullable(facility.getLocation())
        .ifPresent(point -> fhirLocation.position = new FhirPosition(point.getX(), point.getY()));

    fhirLocation.physicalType = new FhirPhysicalType(SITE);

    Optional
        .ofNullable(facility.getGeographicZone())
        .ifPresent(zone ->
            fhirLocation.partOf =
                new FhirReference(serviceUrl, LocationController.RESOURCE_PATH, zone.getId()));

    fhirLocation.description = facility.getDescription();

    fhirLocation.status = isTrue(facility.getActive())
        ? Status.ACTIVE.toString()
        : Status.INACTIVE.toString();

    return fhirLocation;
  }

  private void addAlias(String alias) {
    this.alias.add(alias);
  }

  private void addIdentifier(String serviceUrl, String path, UUID uuid) {
    identifier.add(new FhirIdentifier(serviceUrl, path, uuid));
  }

}
