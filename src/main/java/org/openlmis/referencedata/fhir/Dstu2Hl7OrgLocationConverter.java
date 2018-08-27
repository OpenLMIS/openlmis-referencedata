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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Location.LocationPositionComponent;
import org.hl7.fhir.instance.model.Location.LocationStatus;
import org.hl7.fhir.instance.model.Reference;

class Dstu2Hl7OrgLocationConverter extends LocationConverter<org.hl7.fhir.instance.model.Location> {

  @Override
  public org.hl7.fhir.instance.model.Location apply(Location input) {
    org.hl7.fhir.instance.model.Location location = new org.hl7.fhir.instance.model.Location();

    location.setId(input.getId().toString());
    location.setName(input.getName());
    location.setPhysicalType(getPhysicalType(input));
    location.setPartOf(new Reference(input.getPartOf().getReference()));
    getIdentifiers(input).forEach(location::addIdentifier);

    Optional
        .ofNullable(input.getPosition())
        .ifPresent(position -> location.setPosition(getPosition(position)));

    Optional
        .ofNullable(input.getDescription())
        .ifPresent(location::setDescription);

    Optional
        .ofNullable(input.getStatus())
        .ifPresent(status -> location.setStatus(getStatus(status)));

    return location;
  }

  @Override
  void addSystemIdentifier(org.hl7.fhir.instance.model.Location resource,
      String system, UUID value) {
    Identifier identifier = new Identifier();
    identifier.setSystem(system);
    identifier.setValue(value.toString());

    resource.addIdentifier(identifier);
  }

  private LocationPositionComponent getPosition(Position inputPosition) {
    LocationPositionComponent position = new LocationPositionComponent();
    position.setLatitude(new BigDecimal(inputPosition.getLatitude()));
    position.setLongitude(new BigDecimal(inputPosition.getLongitude()));

    return position;
  }

  private CodeableConcept getPhysicalType(Location input) {
    CodeableConcept physicalType = new CodeableConcept();
    input
        .getPhysicalType()
        .getCoding()
        .stream()
        .map(elem -> {
          Coding coding = new Coding();
          coding.setSystem(elem.getSystem());
          coding.setCode(elem.getCode());
          coding.setDisplay(elem.getDisplay());

          return coding;
        })
        .forEach(physicalType::addCoding);

    return physicalType;
  }

  private LocationStatus getStatus(String inputStatus) {
    try {
      return LocationStatus.fromCode(inputStatus);
    } catch (FHIRException exp) {
      throw new IllegalStateException(exp);
    }
  }

  private List<Identifier> getIdentifiers(Location input) {
    return input
        .getIdentifier()
        .stream()
        .map(elem -> {
          Identifier identifier = new Identifier();
          identifier.setSystem(elem.getSystem());
          identifier.setValue(elem.getValue());

          return identifier;
        })
        .collect(Collectors.toList());
  }

}
